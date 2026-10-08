# AImanage Learning Engine

## Initial implementation
Local telemetry capture, bounded history, rule-based charging temperature/thermal/discharge assessments, and clear-history control.

## Limitations
- Captures only when requested from the UI; no periodic background collection yet.
- Battery discharge samples are not screen-off-only and cannot attribute consumption to specific apps.
- This is deterministic analysis, not a trained ML model or conversational LLM.
- Device samples remain local; avoid storing notification content, call history or contacts.
- The 40 C battery threshold is a configurable future policy, not a universal safety cutoff.
- Never automatically stop other apps or disable Android thermal protection.

## Next steps
1. GitHub Actions build and compile verification.
2. Migrate history to Room with retention settings and export/delete.
3. Add battery/thermal alerts via notification channels and scheduled work respecting Doze.
4. Capture screen-on/off events for actual standby session attribution.
5. Add explainable baseline statistics with sufficient-sample checks and confidence intervals.
6. Permission-gated notification aggregation; content collection off by default.
7. Only then add optional local language model to explain validated tool readings.
