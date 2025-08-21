import React, { useEffect, useRef, useState } from 'react';

export interface Shelf {
  fc_hz: number;
  gain_db: number;
}

export interface Peak {
  fc_hz: number;
  gain_db: number;
  q: number;
}

export interface Eq {
  low_shelf: Shelf;
  peaks: Peak[];
  high_shelf: Shelf;
  tilt_db: number;
}

export interface Modulation {
  enabled: boolean;
  rate_hz: number;
  depth_db: number;
  mode: string;
  fc_drift_oct: number;
  jitter: number;
}

export interface Limiter {
  ceiling_dbfs: number;
  lookahead_ms: number;
  release_ms: number;
}

export interface Compressor {
  enabled: boolean;
  ratio: number;
  threshold_dbfs: number;
  attack_ms: number;
  release_ms: number;
}

export interface DynamicsSettings {
  pregain_db: number;
  limiter: Limiter;
  compressor?: Compressor;
}

export interface Preset {
  version: number;
  name: string;
  eq: Eq;
  modulation: Modulation;
  dynamics: DynamicsSettings;
}

const defaultPreset: Preset = {
  version: 1,
  name: 'Default',
  eq: {
    low_shelf: { fc_hz: 200, gain_db: 0 },
    peaks: [
      { fc_hz: 1000, gain_db: 0, q: 1 },
      { fc_hz: 2000, gain_db: 0, q: 1 },
      { fc_hz: 3000, gain_db: 0, q: 1 },
    ],
    high_shelf: { fc_hz: 6000, gain_db: 0 },
    tilt_db: 0,
  },
  modulation: {
    enabled: false,
    rate_hz: 2,
    depth_db: 0.5,
    mode: 'gain',
    fc_drift_oct: 0,
    jitter: 0.1,
  },
  dynamics: {
    pregain_db: 0,
    limiter: { ceiling_dbfs: -1, lookahead_ms: 5, release_ms: 100 },
    compressor: { enabled: false, ratio: 1.3, threshold_dbfs: -24, attack_ms: 15, release_ms: 120 },
  },
};

const LOCAL_KEY = 'user-presets';

function loadUserPresets(): Record<string, Preset> {
  try {
    const data = localStorage.getItem(LOCAL_KEY);
    return data ? JSON.parse(data) : {};
  } catch {
    return {};
  }
}

function saveUserPresets(presets: Record<string, Preset>) {
  localStorage.setItem(LOCAL_KEY, JSON.stringify(presets));
}

const FilterScreen: React.FC = () => {
  const [preset, setPreset] = useState<Preset>(defaultPreset);
  const [bypass, setBypass] = useState(false);
  const [userPresets, setUserPresets] = useState<Record<string, Preset>>({});
  const [selectedName, setSelectedName] = useState('Default');
  const fileRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    setUserPresets(loadUserPresets());
  }, []);

  const handleSave = () => {
    const updated = { ...userPresets, [preset.name]: preset };
    setUserPresets(updated);
    saveUserPresets(updated);
  };

  const handleSelectPreset = (name: string) => {
    if (name === 'Default') {
      setPreset(defaultPreset);
      setSelectedName('Default');
    } else {
      const p = userPresets[name];
      if (p) {
        setPreset(p);
        setSelectedName(name);
      }
    }
  };

  const handleImport = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    const reader = new FileReader();
    reader.onload = () => {
      try {
        const text = reader.result as string;
        const imported: Preset = JSON.parse(text);
        setPreset(imported);
        setSelectedName(imported.name);
        const updated = { ...userPresets, [imported.name]: imported };
        setUserPresets(updated);
        saveUserPresets(updated);
      } catch {
        console.error('Invalid preset file');
      }
    };
    reader.readAsText(file);
  };

  const handleExport = () => {
    const data = JSON.stringify(preset, null, 2);
    const blob = new Blob([data], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `${preset.name}.json`;
    a.click();
    URL.revokeObjectURL(url);
  };

  const updatePreset = (updated: Preset) => {
    setPreset(updated);
    if (selectedName !== 'Default') {
      const up = { ...userPresets, [updated.name]: updated };
      setUserPresets(up);
      saveUserPresets(up);
    }
  };

  return (
    <div>
      <h1>Filter</h1>
      <div>
        <select value={selectedName} onChange={(e) => handleSelectPreset(e.target.value)}>
          <option value="Default">Default</option>
          {Object.keys(userPresets).map((name) => (
            <option key={name} value={name}>{name}</option>
          ))}
        </select>
        <button onClick={handleSave}>Save</button>
        <button onClick={() => fileRef.current?.click()}>Import</button>
        <button onClick={handleExport}>Export</button>
        <input
          type="file"
          accept="application/json"
          ref={fileRef}
          style={{ display: 'none' }}
          onChange={handleImport}
        />
      </div>
      <div>
        <label>
          Bypass
          <input
            type="checkbox"
            checked={bypass}
            onChange={(e) => setBypass(e.target.checked)}
          />
        </label>
      </div>
      <div>
        <label>
          Name
          <input
            type="text"
            value={preset.name}
            onChange={(e) => setPreset({ ...preset, name: e.target.value })}
          />
        </label>
      </div>
      <div>
        <p>Low Shelf Gain: {preset.eq.low_shelf.gain_db.toFixed(1)} dB</p>
        <input
          type="range"
          min={-12}
          max={12}
          step={0.1}
          value={preset.eq.low_shelf.gain_db}
          onChange={(e) => {
            const gain = parseFloat(e.target.value);
            updatePreset({
              ...preset,
              eq: { ...preset.eq, low_shelf: { ...preset.eq.low_shelf, gain_db: gain } },
            });
          }}
        />
      </div>
      <div>
        <p>High Shelf Gain: {preset.eq.high_shelf.gain_db.toFixed(1)} dB</p>
        <input
          type="range"
          min={-12}
          max={12}
          step={0.1}
          value={preset.eq.high_shelf.gain_db}
          onChange={(e) => {
            const gain = parseFloat(e.target.value);
            updatePreset({
              ...preset,
              eq: { ...preset.eq, high_shelf: { ...preset.eq.high_shelf, gain_db: gain } },
            });
          }}
        />
      </div>
      {preset.eq.peaks.map((peak, idx) => (
        <div key={idx}>
          <p>Peak {idx + 1} Gain: {peak.gain_db.toFixed(1)} dB</p>
          <input
            type="range"
            min={-12}
            max={12}
            step={0.1}
            value={peak.gain_db}
            onChange={(e) => {
              const gain = parseFloat(e.target.value);
              const peaks = preset.eq.peaks.slice();
              peaks[idx] = { ...peaks[idx], gain_db: gain };
              updatePreset({ ...preset, eq: { ...preset.eq, peaks } });
            }}
          />
          <p>Peak {idx + 1} Q: {peak.q.toFixed(2)}</p>
          <input
            type="range"
            min={0.1}
            max={10}
            step={0.01}
            value={peak.q}
            onChange={(e) => {
              const q = parseFloat(e.target.value);
              const peaks = preset.eq.peaks.slice();
              peaks[idx] = { ...peaks[idx], q };
              updatePreset({ ...preset, eq: { ...preset.eq, peaks } });
            }}
          />
        </div>
      ))}
      <div>
        <label>
          Modulation
          <input
            type="checkbox"
            checked={preset.modulation.enabled}
            onChange={(e) => updatePreset({
              ...preset,
              modulation: { ...preset.modulation, enabled: e.target.checked },
            })}
          />
        </label>
      </div>
      <div>
        <p>Modulation Rate: {preset.modulation.rate_hz.toFixed(1)} Hz</p>
        <input
          type="range"
          min={0}
          max={10}
          step={0.1}
          value={preset.modulation.rate_hz}
          onChange={(e) => {
            const rate = parseFloat(e.target.value);
            updatePreset({
              ...preset,
              modulation: { ...preset.modulation, rate_hz: rate },
            });
          }}
        />
      </div>
      <div>
        <p>Modulation Depth: {preset.modulation.depth_db.toFixed(1)} dB</p>
        <input
          type="range"
          min={0}
          max={6}
          step={0.1}
          value={preset.modulation.depth_db}
          onChange={(e) => {
            const depth = parseFloat(e.target.value);
            updatePreset({
              ...preset,
              modulation: { ...preset.modulation, depth_db: depth },
            });
          }}
        />
      </div>
      <div>
        <p>Pregain: {preset.dynamics.pregain_db.toFixed(1)} dB</p>
        <input
          type="range"
          min={0}
          max={24}
          step={0.1}
          value={preset.dynamics.pregain_db}
          onChange={(e) => {
            const pg = parseFloat(e.target.value);
            updatePreset({
              ...preset,
              dynamics: { ...preset.dynamics, pregain_db: pg },
            });
          }}
        />
      </div>
      <div>
        <p>Limiter Ceiling: {preset.dynamics.limiter.ceiling_dbfs.toFixed(1)} dBFS</p>
        <input
          type="range"
          min={-40}
          max={0}
          step={0.1}
          value={preset.dynamics.limiter.ceiling_dbfs}
          onChange={(e) => {
            const ceiling = parseFloat(e.target.value);
            updatePreset({
              ...preset,
              dynamics: {
                ...preset.dynamics,
                limiter: { ...preset.dynamics.limiter, ceiling_dbfs: ceiling },
              },
            });
          }}
        />
      </div>
    </div>
  );
};

export default FilterScreen;

