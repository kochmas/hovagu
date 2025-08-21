document.querySelector('#app').textContent = 'Hello, web-app!';

async function startAudio() {
  const ctx = new AudioContext();
  await ctx.audioWorklet.addModule(new URL('./audio/FfpAudioProcessor.ts', import.meta.url));
  const processor = new AudioWorkletNode(ctx, 'ffp-audio-processor');
  const osc = new OscillatorNode(ctx);
  osc.connect(processor).connect(ctx.destination);
  osc.start();
}

startAudio();
