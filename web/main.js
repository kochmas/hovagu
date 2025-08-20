const fileInput = document.getElementById('file');
const playBtn = document.getElementById('play');
const pauseBtn = document.getElementById('pause');

let audioContext;
let source;
let lowShelf, peak1, peak2, highShelf;
let audioElement;

fileInput.onchange = async () => {
  const file = fileInput.files[0];
  if (!file) return;

  if (!audioContext) {
    audioContext = new (window.AudioContext || window.webkitAudioContext)();
  }

  if (audioElement) {
    audioElement.pause();
    audioElement.remove();
  }

  const url = URL.createObjectURL(file);
  audioElement = new Audio(url);
  audioElement.crossOrigin = "anonymous";

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
};
