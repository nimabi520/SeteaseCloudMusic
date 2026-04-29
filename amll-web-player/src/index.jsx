import React, { useState, useEffect, useRef } from 'react';
import { createRoot } from 'react-dom/client';
import { LyricPlayer } from '@applemusic-like-lyrics/react';
import { parseLrc } from '@applemusic-like-lyrics/lyric';
function App() {
    const [state, setState] = useState({
        currentTime: 0,
        lyricLines: [],
    });
    const lastTime = useRef(-1);
    useEffect(() => {
        window.updateFromAndroid = (currentTimeMs, base64Lrc) => {
            let nextState = {};
            if (base64Lrc && base64Lrc !== 'null') {
                try {
                    const lrcText = decodeURIComponent(escape(atob(base64Lrc)));
                    nextState.lyricLines = parseLrc(lrcText) || [];
                } catch(e) { console.error('LRC Load Error', e); }
            }
            if (currentTimeMs !== undefined && currentTimeMs !== lastTime.current) {
                nextState.currentTime = currentTimeMs;
                lastTime.current = currentTimeMs;
            }
            if (Object.keys(nextState).length > 0) {
                setState(prev => ({ ...prev, ...nextState }));
            }
        };
        // Notify native that the js bridge is ready
        if (window.AndroidBridge && window.AndroidBridge.onReady) {
            window.AndroidBridge.onReady();
        }
    }, []);
    return (
        <div style={{width:'100vw', height:'100vh', backgroundColor:'transparent', color:'white', overflow:'hidden'}}>
            {state.lyricLines.length > 0 && (
                <LyricPlayer
                    lyricLines={state.lyricLines}
                    currentTime={state.currentTime}
                    enableSpring={false}
                    enableScale={true}
                    enableBlur={true}
                    alignAnchor="center"
                    alignPosition={0.5}
                />
            )}
        </div>
    );
}
const root = createRoot(document.getElementById('root'));
root.render(<App />);



