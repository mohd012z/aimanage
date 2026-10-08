# Security and Communications Roadmap

## Implemented
- Offline heuristic URL analysis for user-submitted HTTP(S) links
- Limited E.164-style prefix explanation, without identity assertions
- Opt-in TextToSpeech helper (not connected to call events)

## Pending, gated by explicit user permissions
- NotificationListenerService with app allowlist, on-device classification, no content retention by default
- CallScreeningService with user-selected Android call-screening role
- Contacts lookup with READ_CONTACTS permission
- Telegram share-intent analyzer (do not scrape private Telegram data)
- App inventory and permission-risk assessment with Android package visibility restrictions
- Optional safe browsing reputation checks, with consent and data-minimization
- AI assistant with evidence and uncertainty labels

## Safety and accuracy
Never infer a bank/company identity from a caller ID alone.
Never label a URL as safe because basic heuristics found nothing.
No covert notification collection, Telegram account access, or contact uploads.
No automatic blocking based solely on an unverified heuristic.
