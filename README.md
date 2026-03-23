# Crab Critic

A crab that judges your landing page.

Paste a URL. Get a sarcastic analysis, buzzword detection, cliché spotting, and a dynamically expressive SVG crab reacting to your site's vibes.

Roast levels range from **Polite** to **Senior Emacs monk**.

## How it works

```
URL → jsoup scrape → Claude analysis → SVG crab → JSON response
```

1. **jsoup** extracts headings, CTAs, meta descriptions, and body text
2. **Claude API** analyzes the content and generates a structured roast
3. **SvgRenderer** generates a crab whose facial expression matches the site's cringe score
4. Everything returns as a single JSON response with the SVG embedded

## Run locally

### Prerequisites

- Java 21+
- Maven 3.9+
- A Claude API key ([get one here](https://console.anthropic.com/settings/keys))

### Steps

```bash
# Clone
git clone https://github.com/YOUR-USERNAME/crab-critic.git
cd crab-critic

# Set your API key
export CRAB_CLAUDE_API_KEY=sk-ant-your-key-here

# Run
./mvnw spring-boot:run
```

Open [http://localhost:8080](http://localhost:8080) and paste a URL.

### Or test with curl

```bash
curl -X POST http://localhost:8080/api/roast \
  -H "Content-Type: application/json" \
  -d '{"url": "https://example.com", "roastLevel": 3}'
```

## API

### `POST /api/roast`

**Request:**
```json
{
  "url": "https://some-landing-page.com",
  "roastLevel": 3
}
```

**Response:**
```json
{
  "url": "https://some-landing-page.com",
  "product": "AI-powered growth optimization platform",
  "tone": "hype",
  "buzzwords": ["AI-powered", "synergy", "scalable"],
  "cliches": ["Round-number social proof", "Missing logo wall"],
  "vibe": "A word salad with a pricing page.",
  "reaction": "If buzzwords were plankton, this site would sustain an entire reef.",
  "roastLevel": 3,
  "svg": "<svg>...</svg>"
}
```

### `GET /api/health`

Returns `The crab is alive and judgmental.`

## Cost

Each roast costs approximately **$0.003–$0.005** in Claude API usage (Sonnet).
That's roughly 200 roasts per dollar.

## Project structure

```
src/main/java/dev/crabcritic/
├── CrabCriticApplication.java    # Entry point
├── controller/
│   └── CrabController.java       # Single REST endpoint
├── model/
│   ├── RoastRequest.java          # What comes in
│   ├── RoastResponse.java         # What goes out
│   ├── ScrapedContent.java        # What jsoup extracted
│   └── SiteAnalysis.java          # What Claude returned
└── service/
    ├── ScraperService.java        # jsoup HTML extraction
    ├── AnalysisService.java       # Claude API integration
    └── SvgRenderer.java           # Crab SVG generation

src/main/resources/
├── application.yml                # Config
└── static/
    └── index.html                 # Frontend (single file)
```

## License

GNU, do whatever you want with it.

---

*If this crab made you exhale through your nose, a star would make its day.*
https://github.com/Hyudoro/crab-critic
