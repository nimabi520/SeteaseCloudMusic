export function injectMockData() {
  const demoTTML = `
<tt xmlns="http://www.w3.org/ns/ttml">
  <body>
    <div>
      <p begin="00:00.000" end="00:04.000">Setease Cloud Music</p>
      <p begin="00:04.000" end="00:08.000">Apple Music Like Lyrics</p>
      <p begin="00:08.000" end="00:12.000">Integration Demo</p>
    </div>
  </body>
</tt>
`;

  window.dispatchEvent(
    new CustomEvent("scm-native-message", {
      detail: {
        type: "SET_TRACK",
        payload: {
          id: "demo-1",
          title: "Demo Song",
          artist: "Demo Artist",
          album: "Demo Album",
          coverUrl: "https://picsum.photos/512",
          ttml: demoTTML,
        },
      },
    })
  );

  let t = 0;
  let playing = true;
  setInterval(() => {
    if (!playing) return;
    t += 100;
    window.dispatchEvent(
      new CustomEvent("scm-native-message", {
        detail: {
          type: "SET_PLAYBACK",
          payload: { currentTimeMs: t, durationMs: 180000, playing: true },
        },
      })
    );
  }, 100);

  window.__togglePlay = () => {
    playing = !playing;
    window.dispatchEvent(
      new CustomEvent("scm-native-message", {
        detail: {
          type: "SET_PLAYBACK",
          payload: { currentTimeMs: t, durationMs: 180000, playing },
        },
      })
    );
  };
}