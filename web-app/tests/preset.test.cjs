const test = require('node:test');
const assert = require('node:assert');
const fs = require('fs');
const path = require('path');

test('parses preset JSON', () => {
  const presetPath = path.join(__dirname, '..', 'public', 'presets', '01_social_voice.json');
  const data = fs.readFileSync(presetPath, 'utf8');
  const preset = JSON.parse(data);
  assert.equal(preset.name, 'Soziale Stimme');
  assert.ok(Array.isArray(preset.eq.peaks));
});
