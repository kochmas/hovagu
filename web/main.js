const fileInput = document.getElementById('file');
const dirInput = document.getElementById('dir');
const playBtn = document.getElementById('play');
const pauseBtn = document.getElementById('pause');
const playlistEl = document.getElementById('playlist');

let audioContext;
let source;
let lowShelf, peak1, peak2, highShelf;
let audioElement;

let playlist = [];
let currentIndex = 0;

async function loadTrack(item) {
  if (!item) return;

  if (!audioContext) {
    audioContext = new (window.AudioContext || window.webkitAudioContext)();
  }

  if (audioElement) {
    audioElement.pause();
    audioElement.remove();
  }

  const url = item instanceof File ? URL.createObjectURL(item) : item.toString();
  audioElement = new Audio(url);
  audioElement.crossOrigin = 'anonymous';

  source = audioContext.createMediaElementSource(audioElement);

  lowShelf = audioContext.createBiquadFilter();
  lowShelf.type = 'lowshelf';
  lowShelf.frequency.value = 200;

  peak1 = audioContext.createBiquadFilter();
  peak1.type = 'peaking';
  peak1.frequency.value = 1000;
  peak1.Q.value = 1;

  peak2 = audioContext.createBiquadFilter();
  peak2.type = 'peaking';
  peak2.frequency.value = 4000;
  peak2.Q.value = 1;

  highShelf = audioContext.createBiquadFilter();
  highShelf.type = 'highshelf';
  highShelf.frequency.value = 8000;

  source
    .connect(lowShelf)
    .connect(peak1)
    .connect(peak2)
    .connect(highShelf)
    .connect(audioContext.destination);

  document.getElementById('lowGain').oninput = e => {
    lowShelf.gain.value = e.target.value;
  };
  document.getElementById('peak1Gain').oninput = e => {
    peak1.gain.value = e.target.value;
  };
  document.getElementById('peak2Gain').oninput = e => {
    peak2.gain.value = e.target.value;
  };
  document.getElementById('highGain').oninput = e => {
    highShelf.gain.value = e.target.value;
  };

  playBtn.disabled = false;
  pauseBtn.disabled = false;

  playBtn.onclick = () => audioElement.play();
  pauseBtn.onclick = () => audioElement.pause();
}

function renderPlaylist() {
  playlistEl.innerHTML = '';
  playlist.forEach((item, idx) => {
    const li = document.createElement('li');
    li.textContent = item instanceof File ? item.name : item.toString();
    if (idx === currentIndex) li.style.fontWeight = 'bold';
    li.onclick = async () => {
      currentIndex = idx;
      await loadTrack(playlist[currentIndex]);
      renderPlaylist();
    };
    playlistEl.appendChild(li);
  });
}

fileInput.onchange = async () => {
  const file = fileInput.files[0];
  if (!file) return;
  playlist = [file];
  currentIndex = 0;
  await loadTrack(file);
  renderPlaylist();
};

dirInput.onchange = async () => {
  playlist = [];
  currentIndex = 0;
  const files = Array.from(dirInput.files);

  const fileMap = new Map();
  files.forEach(f => fileMap.set(f.webkitRelativePath, f));

  for (const f of files) {
    if (/\.m3u8?$/i.test(f.name)) {
      const text = await f.text();
      const lines = text.split(/\r?\n/).map(l => l.trim()).filter(l => l && !l.startsWith('#'));
      const baseDir = f.webkitRelativePath.substring(0, f.webkitRelativePath.lastIndexOf('/') + 1);
      for (const line of lines) {
        if (/^https?:\/\//i.test(line)) {
          try {
            playlist.push(new URL(line));
          } catch (_) {}
        } else {
          const relPath = (baseDir + line).replace(/\/g, '/');
          const found = fileMap.get(relPath) || fileMap.get(line);
          if (found) playlist.push(found);
        }
      }
    } else if (f.type.startsWith('audio/')) {
      playlist.push(f);
    }
  }

  await loadTrack(playlist[currentIndex]);
  renderPlaylist();
};
