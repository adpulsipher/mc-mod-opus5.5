#!/usr/bin/env python3
"""Synthesizes every custom sound effect and the music disc for Echoes of the Past.

Run from the repository root:  python3 tools/gen_sounds.py
Requires numpy, scipy and soundfile (with Ogg Vorbis support).
"""
import os

import numpy as np
import soundfile as sf
from scipy.signal import butter, fftconvolve, sosfilt

SR = 44100
OUT = os.path.join(os.path.dirname(__file__), "..", "src", "main", "resources", "assets", "echoes_of_the_past", "sounds")
rng = np.random.default_rng(2026)


def t_axis(duration):
    return np.arange(int(SR * duration)) / SR


def env(duration, attack=0.01, release=0.3, curve=2.0):
    t = t_axis(duration)
    a = np.clip(t / max(attack, 1e-4), 0, 1)
    r = np.clip((duration - t) / max(release, 1e-4), 0, 1) ** curve
    return a * r


def decay(duration, rate):
    return np.exp(-t_axis(duration) * rate)


def lowpass(x, cutoff, order=4):
    return sosfilt(butter(order, cutoff, "low", fs=SR, output="sos"), x)


def highpass(x, cutoff, order=2):
    return sosfilt(butter(order, cutoff, "high", fs=SR, output="sos"), x)


def bandpass(x, lo, hi, order=2):
    return sosfilt(butter(order, [lo, hi], "band", fs=SR, output="sos"), x)


def reverb(x, seconds=2.2, mix=0.35, brightness=5000):
    n = int(SR * seconds)
    ir = rng.standard_normal(n) * np.exp(-np.arange(n) / SR * (6.9 / seconds))
    ir = lowpass(ir, brightness)
    ir /= np.sqrt(np.sum(ir ** 2)) + 1e-9
    wet = fftconvolve(x, ir)
    dry = np.concatenate([x, np.zeros(len(wet) - len(x))])
    return dry * (1 - mix) + wet * mix


def normalize(x, peak=0.85):
    m = np.max(np.abs(x)) + 1e-9
    return x / m * peak


def fade_tail(x, seconds=0.05):
    n = min(len(x), int(SR * seconds))
    x[-n:] *= np.linspace(1, 0, n)
    return x


def save(name, x, peak=0.85):
    x = fade_tail(normalize(x, peak))
    path = os.path.join(OUT, name + ".ogg")
    os.makedirs(os.path.dirname(path), exist_ok=True)
    data = x.astype(np.float32)
    # Write in small blocks: libsndfile's Vorbis encoder can crash on very large single writes.
    with sf.SoundFile(path, "w", SR, 1, format="OGG", subtype="VORBIS") as f:
        for i in range(0, len(data), 8192):
            f.write(data[i:i + 8192])


def bell(freq, duration, partials=((1, 1.0), (2.76, 0.5), (5.4, 0.25), (8.93, 0.12)), rate=3.0):
    t = t_axis(duration)
    x = np.zeros_like(t)
    for ratio, amp in partials:
        x += amp * np.sin(2 * np.pi * freq * ratio * t) * np.exp(-t * rate * (0.6 + ratio * 0.25))
    return x * env(duration, 0.002, 0.2)


def saw(freq, t):
    phase = np.cumsum(np.broadcast_to(freq, t.shape)) / SR
    return 2 * (phase % 1.0) - 1


def sine(freq, t):
    phase = np.cumsum(np.broadcast_to(freq, t.shape)) / SR
    return np.sin(2 * np.pi * phase)


def pluck(freq, duration, damping=0.996):
    """Karplus-Strong plucked string."""
    n = int(SR * duration)
    period = int(SR / freq)
    buf = rng.uniform(-1, 1, period)
    out = np.zeros(n)
    for i in range(n):
        out[i] = buf[i % period]
        nxt = buf[(i + 1) % period]
        buf[i % period] = damping * 0.5 * (buf[i % period] + nxt)
    return out * env(duration, 0.001, 0.3)


def formant_voice(freq, duration, formants=((700, 1.0), (1220, 0.5), (2600, 0.25)), vibrato=5.0):
    t = t_axis(duration)
    f = freq * (1 + 0.01 * np.sin(2 * np.pi * vibrato * t))
    src = saw(f, t)
    out = np.zeros_like(t)
    for fc, amp in formants:
        out += amp * bandpass(src, fc * 0.85, fc * 1.15)
    return out


def mix_at(base, x, start):
    i = int(start * SR)
    end = min(len(base), i + len(x))
    base[i:end] += x[: end - i]
    return base


# ------------------------------------------------------------------ effects

def projector_activate():
    d = 2.2
    t = t_axis(d)
    sweep = sine(200 + 900 * (t / d) ** 2, t) * env(d, 0.05, 0.6)
    shimmer = sum(sine(f * (1 + 0.5 * t / d), t) * 0.2 for f in (880, 1320, 1760)) * env(d, 0.4, 0.8)
    noise = bandpass(rng.standard_normal(len(t)), 2000, 6000) * env(d, 0.8, 0.9) * 0.2
    x = sweep + shimmer + noise
    x = mix_at(x, bell(1568, 1.2), 1.4)
    return reverb(x, 2.0, 0.4)


def projector_hum():
    d = 3.0
    t = t_axis(d)
    x = sine(110, t) * 0.6 + sine(110.7, t) * 0.5 + sine(220.3, t) * 0.25 + sine(330, t) * 0.08
    x *= 0.8 + 0.2 * np.sin(2 * np.pi * 0.7 * t)
    x += lowpass(rng.standard_normal(len(t)), 400) * 0.05
    return x * env(d, 0.4, 0.8, 1.0)


def replay_end():
    d = 3.0
    x = np.zeros(int(SR * d))
    for i, f in enumerate((1318, 1046, 880, 659)):
        x = mix_at(x, bell(f, 1.6, rate=2.5) * (0.9 - i * 0.12), i * 0.28)
    return reverb(x, 2.5, 0.45)


def chime(freq):
    x = bell(freq, 1.4, partials=((1, 1.0), (2.4, 0.4), (4.1, 0.2), (6.7, 0.08)), rate=2.2)
    return reverb(x, 1.8, 0.4, 7000)


def extract():
    d = 1.4
    t = t_axis(d)
    crack = highpass(rng.standard_normal(len(t)), 1500) * np.exp(-t * 35)
    x = crack * 0.8
    for i, f in enumerate((1760, 2217, 2637, 3520)):
        x = mix_at(x, bell(f, 0.9, rate=4.0) * 0.4, 0.05 + i * 0.06)
    return reverb(x, 1.6, 0.35, 8000)


def relic_reveal():
    d = 3.0
    t = t_axis(d)
    swell = (sine(98, t) * 0.5 + sine(147, t) * 0.4 + sine(196, t) * 0.3) * env(d, 1.0, 1.2)
    x = swell + bell(392, d, rate=1.2) * 0.8 + bell(587, d, rate=1.5) * 0.4
    return reverb(x, 2.8, 0.5)


def rift_open():
    d = 2.6
    t = t_axis(d)
    tear = bandpass(rng.standard_normal(len(t)), 300, 5000) * env(d, 0.4, 1.2)
    tear *= 0.6 + 0.4 * np.sin(2 * np.pi * (3 + 20 * t / d) * t)
    boom = sine(55 * np.exp(-t * 0.8), t) * np.exp(-np.maximum(t - 0.9, 0) * 2.0) * (t > 0.9)
    x = tear * 0.7 + boom * 1.2
    return reverb(x, 2.5, 0.4, 3000)


def rift_warning():
    d = 2.2
    t = t_axis(d)
    x = sine(233, t) + sine(247, t) + 0.5 * sine(466, t)
    x *= 0.5 + 0.5 * np.sign(np.sin(2 * np.pi * 4 * t))
    return reverb(x * env(d, 0.05, 0.5), 1.5, 0.35)


def ping():
    d = 1.0
    t = t_axis(d)
    x = sine(1480, t) * np.exp(-t * 6) + 0.3 * sine(2960, t) * np.exp(-t * 9)
    return reverb(x, 1.2, 0.45, 9000)


def horn():
    d = 2.0
    t = t_axis(d)
    f = 146.8 * (1 + 0.03 * np.minimum(t / 0.15, 1)) * (1 + 0.004 * np.sin(2 * np.pi * 5 * t))
    x = lowpass(saw(f, t), 1400) * env(d, 0.12, 0.5)
    x = mix_at(x, lowpass(saw(np.full(len(t), 220.0)[: int(SR * 0.9)], t[: int(SR * 0.9)]), 1600) * env(0.9, 0.08, 0.4) * 0.6, 1.0)
    return reverb(x, 3.0, 0.5, 3500)


def cheer():
    d = 2.6
    t = t_axis(d)
    x = np.zeros(len(t))
    for _ in range(18):
        f = rng.uniform(180, 420)
        voice = formant_voice(f, d, formants=((rng.uniform(600, 900), 1.0), (rng.uniform(1100, 1500), 0.5)), vibrato=rng.uniform(4, 8))
        start = rng.uniform(0, 0.4)
        amp = env(d, rng.uniform(0.05, 0.3), rng.uniform(0.4, 1.2))
        x += np.roll(voice * amp, int(start * SR)) * rng.uniform(0.3, 1.0)
    x += bandpass(rng.standard_normal(len(t)), 500, 3000) * env(d, 0.2, 1.0) * 0.4
    return reverb(x, 2.8, 0.55, 4000)


def chant():
    d = 3.5
    x = np.zeros(int(SR * d))
    for f in (110, 164.8, 220):
        x += formant_voice(f, d, formants=((650, 1.0), (1080, 0.45), (2650, 0.2))) * env(d, 0.6, 1.0)
    return reverb(x, 3.5, 0.6, 3000)


def fanfare():
    d = 2.4
    x = np.zeros(int(SR * d))
    notes = [(0.0, 261.6, 0.3), (0.3, 329.6, 0.3), (0.6, 392.0, 0.3), (0.9, 523.3, 1.2)]
    for start, f, length in notes:
        t = t_axis(length)
        tone = lowpass(saw(np.full(len(t), f), t) + 0.5 * saw(np.full(len(t), f * 1.003), t), 2200) * env(length, 0.03, 0.15)
        x = mix_at(x, tone * 0.6, start)
    return reverb(x, 2.5, 0.45, 5000)


def music_short():
    d = 3.2
    x = np.zeros(int(SR * d))
    for i, f in enumerate((293.7, 349.2, 440.0, 523.3, 440.0, 349.2)):
        x = mix_at(x, pluck(f, 1.6), i * 0.22)
    return reverb(x, 2.4, 0.45)


def whisper(seed):
    local = np.random.default_rng(seed)
    d = 2.2
    t = t_axis(d)
    noise = local.standard_normal(len(t))
    x = np.zeros(len(t))
    for i in range(6):
        center = local.uniform(800, 3000)
        band = bandpass(noise, center * 0.8, center * 1.2)
        gate = np.clip(np.sin(2 * np.pi * local.uniform(2, 5) * t + local.uniform(0, 6)), 0, 1) ** 2
        x += band * gate
    return reverb(x * env(d, 0.2, 0.8), 2.0, 0.5, 6000)


def moan(base, glide, d=2.2):
    t = t_axis(d)
    f = base * (1 + glide * np.sin(np.pi * t / d)) * (1 + 0.02 * np.sin(2 * np.pi * 6 * t))
    x = sine(f, t) + 0.4 * sine(f * 2.01, t) + 0.15 * sine(f * 3.02, t)
    x += bandpass(rng.standard_normal(len(t)), 300, 1200) * 0.15
    return reverb(x * env(d, 0.3, 0.9), 2.5, 0.55, 3000)


def lingerer_death():
    d = 2.6
    t = t_axis(d)
    f = 330 * np.exp(-t * 0.7)
    x = sine(f, t) + 0.4 * sine(f * 1.5, t)
    x *= env(d, 0.02, 1.4)
    for i, fr in enumerate((1760, 2349, 2794)):
        x = mix_at(x, bell(fr, 1.0, rate=3) * 0.2, 0.8 + i * 0.15)
    return reverb(x, 2.5, 0.5)


def phase():
    d = 0.9
    t = t_axis(d)
    x = bandpass(rng.standard_normal(len(t)), 600, 4000) * (t / d) ** 2
    x = x * env(d, 0.01, 0.08)
    x += sine(880 * (1 + t), t) * (t / d) ** 3 * 0.3
    return reverb(x, 1.2, 0.4)


def attune():
    d = 1.6
    x = np.zeros(int(SR * d))
    for i, f in enumerate((1046.5, 1318.5, 1568.0, 2093.0)):
        x = mix_at(x, bell(f, 0.9, rate=3.5) * 0.6, i * 0.09)
    return reverb(x, 1.6, 0.4, 9000)


def wyrm_roar(d=2.8, drop=0.5):
    t = t_axis(d)
    f = 70 * (1 + 0.6 * np.sin(np.pi * t / d)) * np.exp(-t * drop * 0.3)
    growl = saw(f, t) * (0.7 + 0.3 * np.sin(2 * np.pi * 23 * t))
    growl = np.tanh(lowpass(growl, 900) * 3)
    breath = bandpass(rng.standard_normal(len(t)), 200, 2500) * 0.5
    x = (growl + breath) * env(d, 0.12, 0.9)
    return reverb(x, 3.0, 0.45, 2500)


def wyrm_death():
    d = 4.5
    x = wyrm_roar(3.2, 1.5)
    x = np.concatenate([x, np.zeros(int(SR * 2))])
    for i, f in enumerate((1318, 1568, 1976, 2637, 3136)):
        x = mix_at(x, bell(f, 1.8, rate=1.8) * 0.25, 1.6 + i * 0.25)
    return reverb(x[: int(SR * (d + 2))], 3.0, 0.35)


# ------------------------------------------------------------------ music disc

def music_disc():
    """'The Last Chronicler': a slow D dorian piece of pads, harp and bells, about 96 seconds long."""
    bpm = 72
    beat = 60 / bpm
    bars = 28
    d = bars * 4 * beat + 4
    n = int(SR * d)
    x = np.zeros(n)
    t_all = t_axis(d)

    chords = [
        (146.8, 174.6, 220.0, 293.7),  # Dm
        (130.8, 164.8, 196.0, 261.6),  # C
        (116.5, 146.8, 174.6, 233.1),  # Bb
        (130.8, 164.8, 196.0, 246.9),  # C/B-ish colour (dorian 6th)
    ]
    melody_scale = [293.7, 329.6, 349.2, 392.0, 440.0, 493.9, 523.3, 587.3, 659.3]
    local = np.random.default_rng(7)

    # Pads
    for bar in range(bars):
        chord = chords[bar % len(chords)]
        start = bar * 4 * beat
        length = 4 * beat + 1.5
        t = t_axis(length)
        pad = np.zeros(len(t))
        for f in chord:
            pad += saw(np.full(len(t), f), t) * 0.25 + saw(np.full(len(t), f * 1.004), t) * 0.25
        pad = lowpass(pad, 900 + 500 * np.sin(bar / bars * np.pi)) * env(length, 1.2, 1.5, 1.0)
        dynamic = 0.35 if bar < 4 or bar >= bars - 3 else 0.55
        x = mix_at(x, pad * dynamic, start)

    # Harp arpeggios from bar 4
    for bar in range(4, bars - 2):
        chord = chords[bar % len(chords)]
        for i in range(8):
            f = chord[(i * 2 + (i // 4)) % 4] * (2 if i % 3 == 0 else 1)
            x = mix_at(x, pluck(f, 1.8, 0.997) * 0.35, bar * 4 * beat + i * beat / 2)

    # Bell melody from bar 8, phrased in four-bar lines
    for phrase in range(8, bars - 4, 4):
        idx = local.integers(3, 6)
        for note in range(6):
            idx = int(np.clip(idx + local.choice([-2, -1, 1, 2]), 0, len(melody_scale) - 1))
            start = phrase * 4 * beat + note * beat * (1.5 if note % 2 else 1.0) * 1.3
            x = mix_at(x, bell(melody_scale[idx], 2.5, rate=1.4) * 0.35, start)

    # A low drone and wind
    x += (sine(73.4, t_all) * 0.18 + lowpass(local.standard_normal(n), 300) * 0.03) * env(d, 4.0, 5.0, 1.0)
    return reverb(x, 4.0, 0.4, 4500)


# ------------------------------------------------------------------ expansion: bosses and combat

def boss_manifest():
    d = 4.0
    t = t_axis(d)
    rise = sine(55 + 110 * (t / d) ** 2, t) * env(d, 1.5, 0.8) * 0.7
    choir = np.zeros(len(t))
    for f in (130.8, 196.0, 261.6):
        choir += formant_voice(f, d, formants=((600, 1.0), (1000, 0.4), (2500, 0.2))) * env(d, 2.0, 1.0) * 0.4
    shimmer = bandpass(rng.standard_normal(len(t)), 3000, 9000) * env(d, 2.5, 0.8) * 0.2
    x = rise + choir + shimmer
    x = mix_at(x, bell(98, 2.5, rate=1.0) * 1.2, 3.2)
    return reverb(x, 3.5, 0.5, 3500)


def king_decree():
    d = 2.6
    t = t_axis(d)
    x = np.zeros(len(t))
    for f in (98.0, 146.8, 196.0):
        x += lowpass(saw(np.full(len(t), f) * (1 + 0.003 * np.sin(2 * np.pi * 5 * t)), t), 1200) * env(d, 0.2, 1.0) * 0.4
    x = mix_at(x, bell(196, 2.0, rate=1.5) * 0.8, 0.0)
    return reverb(x, 3.0, 0.5, 3000)


def colossus_slam():
    d = 1.8
    t = t_axis(d)
    thud = sine(45 * np.exp(-t * 1.5), t) * np.exp(-t * 3.5)
    crunch = lowpass(rng.standard_normal(len(t)), 1800) * np.exp(-t * 7) * 0.8
    clank = bell(310, d, rate=6.0) * 0.4
    return reverb(thud * 1.4 + crunch + clank, 1.8, 0.35, 2500)


def colossus_groan():
    d = 2.4
    t = t_axis(d)
    f = 55 * (1 + 0.15 * np.sin(2 * np.pi * 0.6 * t))
    metal = lowpass(saw(f, t), 700) * env(d, 0.3, 0.8)
    screech = bandpass(saw(f * 7.1, t), 500, 1500) * env(d, 0.6, 0.6) * 0.3
    return reverb(metal + screech, 2.0, 0.4, 2000)


def hierophant_cast():
    d = 2.0
    t = t_axis(d)
    x = np.zeros(len(t))
    for i, f in enumerate((523.3, 659.3, 784.0, 1046.5)):
        x = mix_at(x, bell(f, 1.6, rate=2.0) * 0.5, i * 0.08)
    x += sine(261.6, t) * env(d, 0.5, 1.0) * 0.4
    return reverb(x, 3.0, 0.55, 8000)


def hierophant_lance():
    d = 1.4
    t = t_axis(d)
    zap = sine(2400 * np.exp(-t * 3) + 200, t) * np.exp(-t * 4)
    air = bandpass(rng.standard_normal(len(t)), 2000, 8000) * np.exp(-t * 5) * 0.6
    return reverb(zap + air, 1.8, 0.4, 9000)


def bolt_fire():
    d = 0.6
    t = t_axis(d)
    x = sine(900 * np.exp(-t * 4) + 300, t) * np.exp(-t * 8)
    x += bandpass(rng.standard_normal(len(t)), 1500, 6000) * np.exp(-t * 12) * 0.5
    return reverb(x, 0.8, 0.3, 8000)


def showcase_build():
    d = 2.2
    x = np.zeros(int(SR * d))
    for i, f in enumerate((392.0, 523.3, 659.3, 784.0)):
        x = mix_at(x, bell(f, 1.4, rate=2.2) * 0.6, i * 0.12)
    t = t_axis(d)
    x += lowpass(rng.standard_normal(len(t)), 400) * env(d, 0.05, 1.0) * 0.3
    return reverb(x, 2.2, 0.4, 7000)


def expansion():
    save("boss/manifest", boss_manifest())
    save("boss/king_decree", king_decree())
    save("boss/colossus_slam", colossus_slam())
    save("boss/colossus_groan", colossus_groan())
    save("boss/hierophant_cast", hierophant_cast())
    save("boss/hierophant_lance", hierophant_lance())
    save("boss/starfall", starfall_simple())
    save("combat/bolt_fire", bolt_fire())
    save("showcase/build", showcase_build())


def starfall_simple():
    d = 1.8
    t = t_axis(d)
    whistle = sine(1800 * np.exp(-t * 2.5) + 300, t) * env(d, 0.02, 1.2) * 0.35
    hit = np.zeros(len(t))
    hit = mix_at(hit, bell(880, 1.4, rate=3.0) * 0.8, 0.35)
    return reverb(whistle + hit, 2.0, 0.45, 9000)


def main():
    save("projector/activate", projector_activate())
    save("projector/hum", projector_hum(), 0.6)
    save("replay/end", replay_end())
    save("echo/chime1", chime(1318.5))
    save("echo/chime2", chime(1568.0))
    save("echo/chime3", chime(1975.5))
    save("echo/extract", extract())
    save("replay/relic_reveal", relic_reveal())
    save("replay/rift_open", rift_open())
    save("replay/rift_warning", rift_warning())
    save("echo/ping", ping())
    save("replay/horn", horn())
    save("replay/cheer", cheer())
    save("replay/chant", chant())
    save("replay/fanfare", fanfare())
    save("replay/music", music_short())
    save("replay/whisper1", whisper(11))
    save("replay/whisper2", whisper(12))
    save("lingerer/ambient1", moan(220, 0.25))
    save("lingerer/ambient2", moan(185, -0.2))
    save("lingerer/death", lingerer_death())
    save("lingerer/phase", phase())
    save("echo/attune", attune())
    save("wyrm/roar", wyrm_roar())
    save("wyrm/death", wyrm_death())
    save("music/echoes", music_disc(), 0.8)
    expansion()
    print("Sounds written.")


if __name__ == "__main__":
    import sys
    if "--expansion" in sys.argv:
        expansion()
        print("Expansion sounds written.")
    else:
        main()
