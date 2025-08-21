const test = require('node:test');
const assert = require('node:assert');

global.sampleRate = 48000;
global.AudioWorkletProcessor = class {};
let ProcessorCtor;
global.registerProcessor = (_name, ctor) => { ProcessorCtor = ctor; };

require('../test-build/FfpAudioProcessor.js');

test('processes audio within bounds', () => {
  const proc = new ProcessorCtor();
  const input = [[new Float32Array([0.5, -0.5, 0.25, -0.25])]];
  const output = [[new Float32Array(4)]];
  const ok = proc.process(input, output);
  assert.ok(ok);
  for (const v of output[0][0]) {
    assert.ok(v <= 1 && v >= -1);
  }
});
