"""Builds the Watch Demo media in public/demo/ from screenshots of the live demo.

Usage:
    python scripts/build_demo_media.py <screenshots-dir>

<screenshots-dir> must hold one image per tour step, named as in STEPS below
(dashboard.jpg, farms.jpg, ...), taken at roughly 16:9 in the light theme while
logged in to the live demo (farmer for every step except the marketplace,
which is the buyer's view). Requires Pillow.

Outputs:
    public/demo/tour-<name>.webp  one still per Guided tour step (1280x720)
    public/demo/walkthrough.webp  animated walkthrough for the Video tab (960x540)
"""
import sys
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

STEPS = [
    ("dashboard", "Your farm business at a glance"),
    ("farms", "Farms with live weather"),
    ("crops", "Track crops and livestock"),
    ("inventory", "Inventory in kilograms"),
    ("finance", "Expenses, revenue and profit"),
    ("marketplace", "Buyers order across organisations"),
    ("orders", "Escrow-protected payments"),
]

OUT = Path(__file__).resolve().parent.parent / "public" / "demo"
GREEN = (8, 120, 63)
INK = (23, 49, 42)
HOLD_MS, FADE_FRAMES, FADE_MS = 2600, 6, 60


def font(size, bold=False):
    for name in (("segoeuib.ttf", "DejaVuSans-Bold.ttf") if bold else ("segoeui.ttf", "DejaVuSans.ttf")):
        try:
            return ImageFont.truetype(name, size)
        except OSError:
            continue
    return ImageFont.load_default()


def to_16_9(img, width):
    """Centre-crop to 16:9, then scale to the given width."""
    w, h = img.size
    if w / h > 16 / 9:
        new_w = round(h * 16 / 9)
        img = img.crop(((w - new_w) // 2, 0, (w - new_w) // 2 + new_w, h))
    else:
        new_h = round(w * 9 / 16)
        img = img.crop((0, 0, w, new_h))
    return img.resize((width, round(width * 9 / 16)), Image.LANCZOS)


def with_caption(img, index, title):
    """Adds a step pill and title bar along the bottom of a walkthrough frame."""
    frame = img.copy()
    draw = ImageDraw.Draw(frame, "RGBA")
    w, h = frame.size
    bar = 56
    draw.rectangle((0, h - bar, w, h), fill=(255, 255, 255, 235))
    draw.line((0, h - bar, w, h - bar), fill=(223, 233, 228, 255), width=1)
    step = f"{index + 1} / {len(STEPS)}"
    pill_font, title_font = font(15, bold=True), font(20, bold=True)
    pw = draw.textlength(step, font=pill_font) + 24
    draw.rounded_rectangle((20, h - bar + 14, 20 + pw, h - 14), radius=14, fill=GREEN)
    draw.text((32, h - bar / 2), step, font=pill_font, fill="white", anchor="lm")
    draw.text((36 + pw, h - bar / 2), title, font=title_font, fill=INK, anchor="lm")
    return frame


def title_card(size):
    card = Image.new("RGB", size, (237, 248, 241))
    draw = ImageDraw.Draw(card)
    w, h = size
    draw.text((w / 2, h / 2 - 28), "AgriConnect", font=font(56, bold=True), fill=GREEN, anchor="mm")
    draw.text((w / 2, h / 2 + 30), "A 30-second tour of the live demo", font=font(24), fill=INK, anchor="mm")
    return card


def main(src_dir):
    src = Path(src_dir)
    OUT.mkdir(parents=True, exist_ok=True)
    shots = []
    for name, _ in STEPS:
        path = next((p for p in src.glob(f"{name}.*")), None)
        if path is None:
            sys.exit(f"Missing screenshot: {name}.* in {src}")
        shots.append(Image.open(path).convert("RGB"))

    for (name, _), shot in zip(STEPS, shots):
        to_16_9(shot, 1280).save(OUT / f"tour-{name}.webp", "WEBP", quality=82, method=6)

    stills = [title_card((960, 540))]
    stills += [with_caption(to_16_9(shot, 960), i, title) for i, ((_, title), shot) in enumerate(zip(STEPS, shots))]

    frames, durations = [], []
    for i, still in enumerate(stills):
        frames.append(still)
        durations.append(1800 if i == 0 else HOLD_MS)
        nxt = stills[(i + 1) % len(stills)]
        for f in range(1, FADE_FRAMES + 1):
            frames.append(Image.blend(still, nxt, f / (FADE_FRAMES + 1)))
            durations.append(FADE_MS)

    frames[0].save(OUT / "walkthrough.webp", "WEBP", save_all=True, append_images=frames[1:],
                   duration=durations, loop=0, quality=70, method=6)
    for p in sorted(OUT.glob("*.webp")):
        print(f"{p.name}: {p.stat().st_size / 1024:.0f} KB")


if __name__ == "__main__":
    if len(sys.argv) != 2:
        sys.exit(__doc__)
    main(sys.argv[1])
