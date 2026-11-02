# Watch Demo media

Used by `src/components/DemoModal.jsx`:

- `walkthrough.webp`: animated walkthrough shown in the **Video walkthrough** tab
- `tour-*.webp`: one still per **Guided tour** step

All of them are real screenshots of the live demo account (`POST /api/auth/demo`).
To refresh them after a UI change:

1. Open the live demo from the landing page ("Try as a farmer"), light theme,
   in a roughly 16:9 window.
2. Take one screenshot per step, named `dashboard`, `farms` (with weather
   loaded), `crops`, `inventory`, `finance` and `orders`. Then use "Try as a
   buyer" and take `marketplace`.
3. Run `python scripts/build_demo_media.py <folder-with-screenshots>` from
   `frontend/` (needs Pillow).
