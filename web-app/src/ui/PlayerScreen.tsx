import React, { useEffect, useRef, useState } from 'react';

export interface Track {
  id: number;
  title: string;
  url: string;
  duration: number; // seconds
}

interface PlayerScreenProps {
  tracks: Track[];
}

const PlayerScreen: React.FC<PlayerScreenProps> = ({ tracks }) => {
  const audioRef = useRef<HTMLAudioElement>(null);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [isPlaying, setIsPlaying] = useState(false);
  const [progress, setProgress] = useState(0);
  const [countdown, setCountdown] = useState(0);

  useEffect(() => {
    let interval: NodeJS.Timeout | undefined;
    if (isPlaying) {
      const total = tracks.slice(currentIndex).reduce((sum, t) => sum + t.duration, 0);
      setCountdown(total);
      interval = setInterval(() => {
        setCountdown((c) => {
          if (c <= 1) {
            handleStop();
            return 0;
          }
          return c - 1;
        });
      }, 1000);
      audioRef.current?.play();
    }
    return () => {
      if (interval) clearInterval(interval);
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isPlaying]);

  const handleStart = () => {
    if (!tracks.length) return;
    setIsPlaying(true);
  };

  const handleStop = () => {
    setIsPlaying(false);
    if (audioRef.current) {
      audioRef.current.pause();
      audioRef.current.currentTime = 0;
    }
    setCurrentIndex(0);
    setProgress(0);
  };

  const handleTimeUpdate = () => {
    const audio = audioRef.current;
    if (!audio) return;
    setProgress((audio.currentTime / audio.duration) * 100);
  };

  const handleEnded = () => {
    if (currentIndex < tracks.length - 1) {
      const next = currentIndex + 1;
      setCurrentIndex(next);
      setProgress(0);
      if (audioRef.current) {
        audioRef.current.src = tracks[next].url;
        audioRef.current.play();
      }
    } else {
      handleStop();
    }
  };

  return (
    <div>
      <h1>Player</h1>
      <ul>
        {tracks.map((t, idx) => (
          <li key={t.id} style={{ fontWeight: idx === currentIndex ? 'bold' : 'normal' }}>
            {t.title}
          </li>
        ))}
      </ul>
      <div>
        <button onClick={handleStart} disabled={isPlaying}>
          Start
        </button>
        <button onClick={handleStop} disabled={!isPlaying}>
          Stop
        </button>
      </div>
      {isPlaying && (
        <>
          <progress value={progress} max={100} />
          <div>{countdown}s</div>
        </>
      )}
      <audio
        ref={audioRef}
        src={tracks[currentIndex]?.url}
        onTimeUpdate={handleTimeUpdate}
        onEnded={handleEnded}
      />
    </div>
  );
};

export default PlayerScreen;
