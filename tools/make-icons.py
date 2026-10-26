#!/usr/bin/env python3
"""Final icon: the six-key ring (top key in the brand accent, silver hub), RNS tile.

Same drawing for every asset; only the scale differs, because adaptive layers are
cropped by the platform mask while legacy PNGs are not.
"""
from PIL import Image, ImageDraw
import math, os

RES = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "app/src/main/res")

TILE_TOP, TILE_BOT = (30, 33, 37), (9, 9, 11)
OUTLINE = (198, 203, 209)
SILVER = (216, 221, 226)
ACCENT = (0xCB, 0x20, 0x26)
FACE = (44, 47, 52)

ORBIT = 0.232          # ring radius, as a fraction of the canvas
KEY_R = 0.090          # key radius
HUB_R = 0.062
RIM_FRAC = 0.017       # rim width, so it survives a 48dp draw
ADAPTIVE_WIDTH = 0.56  # of the canvas; the mask keeps 61%, the rest is stroke blur
LEGACY_WIDTH = 0.80

DENSITIES = {"mdpi": 1, "hdpi": 1.5, "xhdpi": 2, "xxhdpi": 3, "xxxhdpi": 4}
SS = 4


def gradient(size):
    img = Image.new("RGB", (size, size))
    d = ImageDraw.Draw(img)
    for y in range(size):
        t = y / (size - 1)
        d.line([(0, y), (size, y)],
               fill=tuple(round(a + (b - a) * t ** 0.9) for a, b in zip(TILE_TOP, TILE_BOT)))
    return img


def rounded_mask(size, radius_frac=0.225):
    m = Image.new("L", (size, size), 0)
    ImageDraw.Draw(m).rounded_rectangle([0, 0, size - 1, size - 1],
                                        radius=int(size * radius_frac), fill=255)
    return m


def circle_mask(size):
    m = Image.new("L", (size, size), 0)
    ImageDraw.Draw(m).ellipse([0, 0, size - 1, size - 1], fill=255)
    return m


def marks(size, mono=False):
    """Six keys on a ring, the top one accent-filled, plus the hub ring."""
    layer = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    d = ImageDraw.Draw(layer)
    c = size / 2
    r = size * KEY_R
    orbit = size * ORBIT
    rim_w = max(2, int(size * RIM_FRAC))
    ink = (255, 255, 255) if mono else SILVER
    for i in range(6):
        a = math.radians(-90 + i * 60)
        x, y = c + orbit * math.cos(a), c + orbit * math.sin(a)
        box = [x - r, y - r, x + r, y + r]
        if i == 0:
            d.ellipse(box, fill=(ACCENT if not mono else ink) + (255,))
        else:
            d.ellipse(box, fill=(FACE if not mono else (255, 255, 255)) + (255,))
            if not mono:
                d.arc(box, 0, 360, fill=ink + (255,), width=rim_w)
    hub = size * HUB_R
    d.arc([c - hub, c - hub, c + hub, c + hub], 0, 360,
          fill=(ink if mono else SILVER) + (255,), width=rim_w)
    bb = layer.getbbox()
    out = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    out.alpha_composite(layer, (int(round(size / 2 - (bb[0] + bb[2]) / 2)),
                                int(round(size / 2 - (bb[1] + bb[3]) / 2))))
    return out


def scaled(mark, size, frac):
    """Fit the mark so its LARGER dimension lands on [frac] of the canvas: the ring is
    taller than it is wide, and a circular mask cuts on the tall axis."""
    bb = mark.getbbox()
    big = max(bb[2] - bb[0], bb[3] - bb[1])
    scale = (size * frac) / big
    inner = int(size * scale)
    small = mark.resize((inner, inner), Image.LANCZOS)
    out = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    off = (size - inner) // 2
    out.alpha_composite(small, (off, off))
    return out


def legacy_icon(size, round_shape=False):
    img = gradient(size).convert("RGBA")
    img.putalpha(circle_mask(size) if round_shape else rounded_mask(size))
    img.alpha_composite(scaled(marks(size), size, LEGACY_WIDTH))
    if not round_shape:
        ImageDraw.Draw(img).rounded_rectangle(
            [3, 3, size - 4, size - 4], radius=int(size * 0.225),
            outline=OUTLINE, width=max(2, size // 220))
    return img


def adaptive_foreground(size):
    return scaled(marks(size), size, ADAPTIVE_WIDTH)


def adaptive_monochrome(size):
    return scaled(marks(size, mono=True), size, ADAPTIVE_WIDTH)


def write(img, rel):
    path = os.path.join(RES, rel)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    img.save(path)
    return path


def build_all():
    out = []
    for name, mult in DENSITIES.items():
        for shape, suffix in ((False, "ic_launcher"), (True, "ic_launcher_round")):
            px = int(48 * mult)
            src = px * SS if px < 200 else px
            out.append(write(legacy_icon(src, round_shape=shape).resize((px, px), Image.LANCZOS),
                             f"mipmap-{name}/{suffix}.png"))
        px = int(108 * mult)
        out.append(write(gradient(px * 2).convert("RGBA").resize((px, px), Image.LANCZOS),
                         f"mipmap-{name}/ic_launcher_background.png"))
        out.append(write(adaptive_foreground(px * SS).resize((px, px), Image.LANCZOS),
                         f"mipmap-{name}/ic_launcher_foreground.png"))
        out.append(write(adaptive_monochrome(px * SS).resize((px, px), Image.LANCZOS),
                         f"mipmap-{name}/ic_launcher_monochrome.png"))
    return out


if __name__ == "__main__":
    for p in build_all():
        print(os.path.relpath(p, RES))
