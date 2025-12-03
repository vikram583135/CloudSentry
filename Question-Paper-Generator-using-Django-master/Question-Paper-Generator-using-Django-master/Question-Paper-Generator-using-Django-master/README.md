# Smart Question Paper Generator (Django)

A Django web application to create, manage and generate examination question papers. Teachers can add questions to a question bank, filter by subject/year/unit, assemble papers with section-wise marks, validate totals, and export the final paper as a PDF.

---

## Features

- Question bank CRUD (add, view, filter, delete by subject/year/unit)
- Generate question papers with configurable sections, question counts and marks
- Validation: total marks must equal requested maximum before PDF generation
- PDF export of generated papers (requires system PDF tool if used)
- Clean, responsive UI using Bootstrap
- Feedback via session/messages and animated notifications

---

## Prerequisites

- Python 3.7+ (project was developed using Python 3.7.8)
- Git (for cloning/pushing to GitHub)
- (Optional) wkhtmltopdf or other PDF backend if your PDF generation requires it
- On Windows: PowerShell or Command Prompt

---

## Quick install (development) — Windows (PowerShell)

Open PowerShell in the project root (where `manage.py` is).

1. Create and activate a virtual environment
```powershell
python -m venv env
# PowerShell activation
.\env\Scripts\Activate.ps1
# (or if using cmd.exe)
# .\env\Scripts\activate.bat
```

2. Upgrade pip and install dependencies
```powershell
python -m pip install --upgrade pip
# If repository has requirements.txt:
pip install -r requirements.txt
# If requirements.txt is missing, generate it after installing packages:
# pip freeze > requirements.txt
```

3. Environment variables (do not hard-code SECRET_KEY in repo)
Set required environment variables (example in PowerShell):
```powershell
$env:DJANGO_SECRET_KEY = "your-secret-key"
$env:DJANGO_DEBUG = "True"    # set to False in production
# If you use other env vars (DB, email), set them similarly.
```
Alternatively use a `.env` file and a loader (django-environ / python-decouple) — recommended for production.

4. Database migrations and superuser
```powershell
python manage.py migrate
python manage.py createsuperuser
```

5. Run development server
```powershell
python manage.py runserver
# Open http://127.0.0.1:8000/ in your browser
```

---

## Usage

- Login (use superuser or teacher account)
- Add questions to the question bank (subject, year, unit, question text, marks, etc.)
- Use the Generate Paper flow:
  - Enter section counts/marks and `max_marks`
  - The view validates that the sum of section totals equals `max_marks`
  - If equal → PDF generation proceeds; if not → show warning and return to generator
- Delete questions:
  - Use `delete.html` to enter filters (subject, year, unit)
  - Results are shown in `deletequestions.html`
  - Use Delete button to remove a question; success message shown as animated notification

---

## Frontend notes

- Templates are in `templates/` (includes `intermediate.html`, `intermediate2.html`, `generatePaper.html`, ...)
- Static files are in `static/`
- If you modify static assets for production:
```powershell
python manage.py collectstatic
```

---

## Git / GitHub

1. Create a `.gitignore` (already included). Ensure it excludes:
```
env/
venv/
db.sqlite3
*.pyc
__pycache__/
media/
```

2. Recommended `.gitattributes` to normalize line endings:
```
* text=auto
```

3. Common workflow to push to GitHub
```powershell
git init
git add -A
git commit -m "Initial commit"
git remote add origin https://github.com/<your-username>/<repo-name>.git
git branch -M main
git push -u origin main
```

Important: Do not commit the virtual environment, local database, large binaries or secrets. If large files were committed accidentally, remove them from history (BFG or git filter-repo).

---

## Production deployment notes

- Set `DEBUG = False` and configure `ALLOWED_HOSTS`.
- Use environment variables for `SECRET_KEY`, DB credentials, and other secrets.
- Use a production database (Postgres recommended).
- Configure static files serving (e.g., WhiteNoise or dedicated CDN).
- If using PDF generation that depends on native binaries (wkhtmltopdf), install those on the server and configure paths.
- Use HTTPS and proper server (gunicorn / uwsgi behind nginx).

---

## Common troubleshooting

- TemplateDoesNotExist: ensure template file exists in `templates/` and `TEMPLATES` DIRS include the project templates folder.
- NameError (e.g., `max_marks` is not defined): check view variables and ensure they are defined before adding to context.
- Messages not showing: ensure `'django.contrib.messages'` is in `INSTALLED_APPS` and `MessageMiddleware` is in `MIDDLEWARE`.
- Static files not loading: run `collectstatic` (production) and ensure `STATICFILES_DIRS`/`STATIC_ROOT` configured.

---

## Recommended improvements

- Move secret settings to environment variables
- Add tests (unit + integration)
- Add CI (GitHub Actions) for linting and tests
- Add Dockerfile for consistent deployment
- Replace SQLite with PostgreSQL for production

---

## Contributing

- Fork the repository
- Create a feature branch
- Open a pull request with a clear description of changes

---

## License

Add a `LICENSE` file in repository root (e.g., MIT). If you want, I can generate a LICENSE file for you.

---

## Contact / Credits

Designed & developed by Kiran and Vidyashree.

If you want, I can generate a ready-to-commit `README.md`, `requirements.txt`, and `LICENSE` files — tell me which license to use.// filepath: c:\Users\kiruk\OneDrive\Desktop\Question-Paper-Generator-using-Django-master\Question-Paper-Generator-using-Django-master\README.md
# Smart Question Paper Generator (Django)

A Django web application to create, manage and generate examination question papers. Teachers can add questions to a question bank, filter by subject/year/unit, assemble papers with section-wise marks, validate totals, and export the final paper as a PDF.

---

## Features

- Question bank CRUD (add, view, filter, delete by subject/year/unit)
- Generate question papers with configurable sections, question counts and marks
- Validation: total marks must equal requested maximum before PDF generation
- PDF export of generated papers (requires system PDF tool if used)
- Clean, responsive UI using Bootstrap
- Feedback via session/messages and animated notifications

---

## Prerequisites

- Python 3.7+ (project was developed using Python 3.7.8)
- Git (for cloning/pushing to GitHub)
- (Optional) wkhtmltopdf or other PDF backend if your PDF generation requires it
- On Windows: PowerShell or Command Prompt

---

## Quick install (development) — Windows (PowerShell)

Open PowerShell in the project root (where `manage.py` is).

1. Create and activate a virtual environment
```powershell
python -m venv env
# PowerShell activation
.\env\Scripts\Activate.ps1
# (or if using cmd.exe)
# .\env\Scripts\activate.bat
```

2. Upgrade pip and install dependencies
```powershell
python -m pip install --upgrade pip
# If repository has requirements.txt:
pip install -r requirements.txt
# If requirements.txt is missing, generate it after installing packages:
# pip freeze > requirements.txt
```

3. Environment variables (do not hard-code SECRET_KEY in repo)
Set required environment variables (example in PowerShell):
```powershell
$env:DJANGO_SECRET_KEY = "your-secret-key"
$env:DJANGO_DEBUG = "True"    # set to False in production
# If you use other env vars (DB, email), set them similarly.
```
Alternatively use a `.env` file and a loader (django-environ / python-decouple) — recommended for production.

4. Database migrations and superuser
```powershell
python manage.py migrate
python manage.py createsuperuser
```

5. Run development server
```powershell
python manage.py runserver
# Open http://127.0.0.1:8000/ in your browser
```

---

## Usage

- Login (use superuser or teacher account)
- Add questions to the question bank (subject, year, unit, question text, marks, etc.)
- Use the Generate Paper flow:
  - Enter section counts/marks and `max_marks`
  - The view validates that the sum of section totals equals `max_marks`
  - If equal → PDF generation proceeds; if not → show warning and return to generator
- Delete questions:
  - Use `delete.html` to enter filters (subject, year, unit)
  - Results are shown in `deletequestions.html`
  - Use Delete button to remove a question; success message shown as animated notification

---

## Frontend notes

- Templates are in `templates/` (includes `intermediate.html`, `intermediate2.html`, `generatePaper.html`, ...)
- Static files are in `static/`
- If you modify static assets for production:
```powershell
python manage.py collectstatic
```

---

## Git / GitHub

1. Create a `.gitignore` (already included). Ensure it excludes:
```
env/
venv/
db.sqlite3
*.pyc
__pycache__/
media/
```

2. Recommended `.gitattributes` to normalize line endings:
```
* text=auto
```

3. Common workflow to push to GitHub
```powershell
git init
git add -A
git commit -m "Initial commit"
git remote add origin https://github.com/<your-username>/<repo-name>.git
git branch -M main
git push -u origin main
```

Important: Do not commit the virtual environment, local database, large binaries or secrets. If large files were committed accidentally, remove them from history (BFG or git filter-repo).

---

## Production deployment notes

- Set `DEBUG = False` and configure `ALLOWED_HOSTS`.
- Use environment variables for `SECRET_KEY`, DB credentials, and other secrets.
- Use a production database (Postgres recommended).
- Configure static files serving (e.g., WhiteNoise or dedicated CDN).
- If using PDF generation that depends on native binaries (wkhtmltopdf), install those on the server and configure paths.
- Use HTTPS and proper server (gunicorn / uwsgi behind nginx).

---

## Common troubleshooting

- TemplateDoesNotExist: ensure template file exists in `templates/` and `TEMPLATES` DIRS include the project templates folder.
- NameError (e.g., `max_marks` is not defined): check view variables and ensure they are defined before adding to context.
- Messages not showing: ensure `'django.contrib.messages'` is in `INSTALLED_APPS` and `MessageMiddleware` is in `MIDDLEWARE`.
- Static files not loading: run `collectstatic` (production) and ensure `STATICFILES_DIRS`/`STATIC_ROOT` configured.

---

## Recommended improvements

- Move secret settings to environment variables
- Add tests (unit + integration)
- Add CI (GitHub Actions) for linting and tests
- Add Dockerfile for consistent deployment
- Replace SQLite with PostgreSQL for production

---

## Contributing

- Fork the repository
- Create a feature branch
- Open a pull request with a clear description of changes

---

## License

Add a `LICENSE` file in repository root (e.g., MIT). If you want, I can generate a LICENSE file for you.

---

## Contact / Credits

Designed & developed by Kiran and Vidyashree.

If you want, I can generate a ready-to-commit `README.md`, `requirements.txt`, and `LICENSE` files — tell me which license to use.