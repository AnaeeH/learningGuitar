import base64
import os

# Chemin vers les images (relatif à l'emplacement du script)
# learningGuitar/database/scripts/createChords.py  <-- script ici
# learningGuitar/database/images/chords/A.png      <-- images ici
IMAGES_DIR = os.path.join(os.path.dirname(__file__), "..", "images", "chords")

# Chemin de sortie du fichier SQL (généré dans le même dossier que le script)
OUTPUT_FILE = os.path.join(os.path.dirname(__file__), "insert_chords.sql")

# -----------------------------------------------------------------------------
# Mapping note -> id (correspond aux INSERT de t_note_nte)
# -----------------------------------------------------------------------------
note_ids = {
    "A": 1,
    "B": 2,
    "C": 3,
    "D": 4,
    "E": 5,
    "F": 6,
    "G": 7,
}

# -----------------------------------------------------------------------------
# Liste complète des accords
# -----------------------------------------------------------------------------
accords = [
    # Accords majeurs
    {"file": "A.png",     "name": "A",     "is_major": True,  "note": "A"},
    {"file": "B.png",     "name": "B",     "is_major": True,  "note": "B"},
    {"file": "C.png",     "name": "C",     "is_major": True,  "note": "C"},
    {"file": "D.png",     "name": "D",     "is_major": True,  "note": "D"},
    {"file": "E.png",     "name": "E",     "is_major": True,  "note": "E"},
    {"file": "F.png",     "name": "F",     "is_major": True,  "note": "F"},
    {"file": "G.png",     "name": "G",     "is_major": True,  "note": "G"},
    # Accords mineurs
    {"file": "Am.png",    "name": "Am",    "is_major": False, "note": "A"},
    {"file": "Bm.png",    "name": "Bm",    "is_major": False, "note": "B"},
    {"file": "Cm.png",    "name": "Cm",    "is_major": False, "note": "C"},
    {"file": "Dm.png",    "name": "Dm",    "is_major": False, "note": "D"},
    {"file": "Em.png",    "name": "Em",    "is_major": False, "note": "E"},
    # Accords spéciaux
    {"file": "Fmaj7.png", "name": "Fmaj7", "is_major": True,  "note": "F"},
]

# -----------------------------------------------------------------------------
# Génération du SQL
# -----------------------------------------------------------------------------
output = []
output.append("-- -----------------------------------------------------------------------------")
output.append("-- DONNÉES : Accords de guitare")
output.append("-- -----------------------------------------------------------------------------")

errors = []

for accord in accords:
    path = os.path.join(IMAGES_DIR, accord["file"])

    # Vérification que le fichier image existe
    if not os.path.exists(path):
        errors.append(f"⚠️  Image introuvable : {path}")
        print(f"⚠️  Image introuvable, accord ignoré : {accord['name']} ({path})")
        continue

    # Lecture et encodage en Base64
    with open(path, "rb") as f:
        image_data = f.read()
        b64 = base64.b64encode(image_data).decode("utf-8")

    # Détection du type MIME
    ext = accord["file"].split(".")[-1].lower()
    mime_types = {"png": "image/png", "jpg": "image/jpeg", "jpeg": "image/jpeg"}
    mime = mime_types.get(ext, "image/png")

    # Construction de la data URL
    data_url = f"data:{mime};base64,{b64}"

    is_major = "true" if accord["is_major"] else "false"
    note_id = note_ids[accord["note"]]

    sql = (
        f"INSERT INTO t_chord_chr (chr_name, chr_diagram, chr_ismajor, chr_nte_id) VALUES\n"
        f"    ('{accord['name']}', '{data_url}', {is_major}, {note_id});"
    )
    output.append(sql)
    print(f"Accord traité : {accord['name']}")

# Écriture du fichier SQL
with open(OUTPUT_FILE, "w", encoding="utf-8") as f:
    f.write("\n\n".join(output))

print(f"\nFichier SQL généré : {OUTPUT_FILE}")

if errors:
    print("\nImages manquantes :")
    for e in errors:
        print(f"   {e}")
    print(f"\nVérifie que tes images sont bien dans : {IMAGES_DIR}")