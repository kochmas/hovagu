/*
 * Audio worklet processor with low/high shelf filters, multiple peaking filters,
 * simple compressor/limiter and an LFO.
 */

class Biquad {
  private b0: number; private b1: number; private b2: number;
  private a1: number; private a2: number;
  private x1 = 0; private x2 = 0; private y1 = 0; private y2 = 0;

  constructor(
    type: 'lowshelf' | 'highshelf' | 'peaking',
    freq: number,
    q: number,
    gain: number,
    sampleRate: number
  ) {
    const A = Math.pow(10, gain / 40);
    const w0 = 2 * Math.PI * freq / sampleRate;
    const cosw0 = Math.cos(w0);
    const sinw0 = Math.sin(w0);
    const alpha = sinw0 / (2 * q);
    let b0, b1, b2, a0, a1, a2;

    switch (type) {
      case 'lowshelf': {
        const beta = Math.sqrt(A) / q;
        b0 = A * ((A + 1) - (A - 1) * cosw0 + beta * sinw0);
        b1 = 2 * A * ((A - 1) - (A + 1) * cosw0);
        b2 = A * ((A + 1) - (A - 1) * cosw0 - beta * sinw0);
        a0 = (A + 1) + (A - 1) * cosw0 + beta * sinw0;
        a1 = -2 * ((A - 1) + (A + 1) * cosw0);
        a2 = (A + 1) + (A - 1) * cosw0 - beta * sinw0;
        break;
      }
      case 'highshelf': {
        const beta = Math.sqrt(A) / q;
        b0 = A * ((A + 1) + (A - 1) * cosw0 + beta * sinw0);
        b1 = -2 * A * ((A - 1) + (A + 1) * cosw0);
        b2 = A * ((A + 1) + (A - 1) * cosw0 - beta * sinw0);
        a0 = (A + 1) - (A - 1) * cosw0 + beta * sinw0;
        a1 = 2 * ((A - 1) - (A + 1) * cosw0);
        a2 = (A + 1) - (A - 1) * cosw0 - beta * sinw0;
        break;
      }
      case 'peaking':
      default: {
        b0 = 1 + alpha * A;
        b1 = -2 * cosw0;
        b2 = 1 - alpha * A;
        a0 = 1 + alpha / A;
        a1 = -2 * cosw0;
        a2 = 1 - alpha / A;
        break;
      }
    }

    this.b0 = b0 / a0;
    this.b1 = b1 / a0;
    this.b2 = b2 / a0;
    this.a1 = a1 / a0;
    this.a2 = a2 / a0;
  }

  process(x: number): number {
    const y = this.b0 * x + this.b1 * this.x1 + this.b2 * this.x2 - this.a1 * this.y1 - this.a2 * this.y2;
    this.x2 = this.x1;
    this.x1 = x;
    this.y2 = this.y1;
    this.y1 = y;
    return y;
  }
}

class Compressor {
  private threshold: number;
  private ratio: number;

  constructor(thresholdDb = -24, ratio = 12) {
    this.threshold = Math.pow(10, thresholdDb / 20);
    this.ratio = ratio;
  }

  process(x: number): number {
    const sign = x >= 0 ? 1 : -1;
    const abs = Math.abs(x);
    if (abs < this.threshold) return x;
    const exceeded = abs - this.threshold;
    const compressed = this.threshold + exceeded / this.ratio;
    return compressed * sign;
  }
}

class LFO {
  private phase = 0;
  constructor(private freq: number, private depth: number, private sampleRate: number) {}

  next(): number {
    const value = Math.sin(this.phase) * this.depth;
    this.phase += 2 * Math.PI * this.freq / this.sampleRate;
    if (this.phase > Math.PI * 2) this.phase -= Math.PI * 2;
    return value;
  }
}

class FfpAudioProcessor extends AudioWorkletProcessor {
  private low: Biquad;
  private high: Biquad;
  private peaks: Biquad[];
  private comp: Compressor;
  private lfo: LFO;

  constructor() {
    super();
    const sr = sampleRate;
    this.low = new Biquad('lowshelf', 200, 0.707, 3, sr);
    this.high = new Biquad('highshelf', 6000, 0.707, 3, sr);
    this.peaks = [
      new Biquad('peaking', 1000, 1, 0, sr),
      new Biquad('peaking', 2000, 1, 0, sr),
      new Biquad('peaking', 3000, 1, 0, sr)
    ];
    this.comp = new Compressor(-12, 4);
    this.lfo = new LFO(2, 0.1, sr);
  }

  process(inputs: Float32Array[][], outputs: Float32Array[][]): boolean {
    const input = inputs[0];
    const output = outputs[0];
    if (!input || !input[0] || !output || !output[0]) return true;
    const chIn = input[0];
    const chOut = output[0];
    for (let i = 0; i < chIn.length; i++) {
      let sample = chIn[i];
      sample = this.low.process(sample);
      sample = this.high.process(sample);
      for (const p of this.peaks) sample = p.process(sample);
      sample += this.lfo.next();
      sample = this.comp.process(sample);
      if (sample > 1) sample = 1; // limiter
      if (sample < -1) sample = -1;
      chOut[i] = sample;
    }
    return true;
  }
}

registerProcessor('ffp-audio-processor', FfpAudioProcessor);

export {};
