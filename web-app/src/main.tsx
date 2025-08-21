import React from 'react';
import ReactDOM from 'react-dom/client';
import PlayerScreen, { Track } from './ui/PlayerScreen';

const tracks: Track[] = [
  { id: 1, title: 'Track A', url: '/audio/track-a.mp3', duration: 30 },
  { id: 2, title: 'Track B', url: '/audio/track-b.mp3', duration: 45 },
];

ReactDOM.createRoot(document.getElementById('app') as HTMLElement).render(
  <React.StrictMode>
    <PlayerScreen tracks={tracks} />
  </React.StrictMode>
);
