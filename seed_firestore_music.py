import json
import urllib.request

project = "still-wellness-cd4d4"
api_key = "AIzaSyCnoSXDuOvVZuIEtZ97zjqqGMrB7aNYZjw"

tracks_update = [
    {
        "id": "med_1",
        "title": "Quiet The Mind",
        "category": "Mindfulness",
        "durationMinutes": 10,
        "description": "Gentle awareness grounding to dissolve racing thoughts and anchor attention in the present moment.",
        "isNarrated": True,
        "audioUrl": "",
        "iconName": "self_improvement",
        "accentColor": "#2DD4BF",
        "baseFrequency": 432.0,
        "secondaryFrequency": 648.0
    },
    {
        "id": "med_2",
        "title": "Celestial Waves",
        "category": "Sleep Preparation",
        "durationMinutes": 15,
        "description": "Deep ambient relaxation designed to slow neural rhythms before sleep.",
        "isNarrated": False,
        "audioUrl": "",
        "iconName": "bedtime",
        "accentColor": "#818CF8",
        "baseFrequency": 174.0,
        "secondaryFrequency": 261.0
    },
    {
        "id": "med_3",
        "title": "Vagus Nerve Release",
        "category": "Stress Relief",
        "durationMinutes": 7,
        "description": "Subtle somatic cues and diaphragmatic breath resonance for rapid nervous system reset.",
        "isNarrated": True,
        "audioUrl": "",
        "iconName": "favorite",
        "accentColor": "#FB7185",
        "baseFrequency": 528.0,
        "secondaryFrequency": 792.0
    },
    {
        "id": "med_4",
        "title": "Deep Focus Flow",
        "category": "Focus",
        "durationMinutes": 20,
        "description": "Alpha-wave binaural tone ambiance for undistracted creative and cognitive immersion.",
        "isNarrated": False,
        "audioUrl": "",
        "iconName": "psychology",
        "accentColor": "#38BDF8",
        "baseFrequency": 396.0,
        "secondaryFrequency": 594.0
    },
    {
        "id": "med_5",
        "title": "Midday Micro-Reset",
        "category": "Short Breaks",
        "durationMinutes": 3,
        "description": "A swift 3-minute clarity recharge between meetings or demanding tasks.",
        "isNarrated": True,
        "audioUrl": "",
        "iconName": "wb_sunny",
        "accentColor": "#FBBF24",
        "baseFrequency": 639.0,
        "secondaryFrequency": 958.0
    }
]

for t in tracks_update:
    url = f"https://firestore.googleapis.com/v1/projects/{project}/databases/(default)/documents/meditation_tracks/{t['id']}?key={api_key}"
    fields = {
        "id": {"stringValue": t["id"]},
        "title": {"stringValue": t["title"]},
        "category": {"stringValue": t["category"]},
        "durationMinutes": {"integerValue": t["durationMinutes"]},
        "description": {"stringValue": t["description"]},
        "isNarrated": {"booleanValue": t["isNarrated"]},
        "audioUrl": {"stringValue": t["audioUrl"]},
        "iconName": {"stringValue": t["iconName"]},
        "accentColor": {"stringValue": t["accentColor"]},
        "baseFrequency": {"doubleValue": t["baseFrequency"]},
        "secondaryFrequency": {"doubleValue": t["secondaryFrequency"]}
    }
    req = urllib.request.Request(
        url,
        data=json.dumps({"fields": fields}).encode("utf-8"),
        headers={"Content-Type": "application/json"},
        method="PATCH"
    )
    with urllib.request.urlopen(req) as resp:
        print(f"Updated in Firestore DB: {t['id']} -> HTTP {resp.status}")

print("Firestore update completed successfully!")
