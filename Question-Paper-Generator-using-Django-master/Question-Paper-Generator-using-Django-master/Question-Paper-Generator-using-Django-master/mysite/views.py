from django.http import HttpResponse
from django.shortcuts import render, redirect, get_object_or_404
from django.contrib.auth import authenticate, login, logout
from loginapp.models import questionBank
from django.contrib.auth.decorators import login_required
from django.views.generic import View
from mysite.utils import render_to_pdf
from django.template.loader import get_template
from io import BytesIO
from xhtml2pdf import pisa
import random
import datetime
from django.contrib import messages


def home(request):
    if request.method == "POST":
        logout(request)
        messages.success(request, "Successfully Logged out!")
        return redirect('home')
    return render(request, 'home.html')


def loginpage(request):
    return render(request, 'login.html')


def view_login(request):
    if request.method == "POST":
        loginusername = request.POST['username']
        loginpass = request.POST['password']

        user = authenticate(username=loginusername, password=loginpass)

        if user is not None:
            login(request, user)
            messages.success(request, "Successfully Logged in!")
            return redirect('index')

    messages.warning(request, "Login Failed! Please Try Again")
    return render(request, 'login.html')


@login_required(login_url='home')
def logout_user(request):
    if request.method == "POST":
        logout(request)
        messages.success(request, "Successfully Logged out!")
        return redirect('home')


@login_required(login_url='login')
def intermediate(request):
    max_marks = request.POST.get('max_marks', '')
    nques = request.POST.get('nques')
    nq1 = int(request.POST.get('nq1', 0))
    nq2 = int(request.POST.get('nq2', 0))
    nq3 = int(request.POST.get('nq3', 0))
    nq4 = int(request.POST.get('nq4', 0))
    nq5 = int(request.POST.get('nq5', 0))
    params = {
        "nques": nques,
        "max_marks": max_marks,
        "nq1": nq1,
        "nq2": nq2,
        "nq3": nq3,
        "nq4": nq4,
        "nq5": nq5,
    }
    return render(request, 'intermediate.html', params)


@login_required(login_url='login')
def intermediate2(request):
    max_marks = request.POST.get('max_marks', '')
    nq1 = int(request.POST.get('nq1', 0))
    nq2 = int(request.POST.get('nq2', 0))
    nq3 = int(request.POST.get('nq3', 0))
    nq4 = int(request.POST.get('nq4', 0))
    nq5 = int(request.POST.get('nq5', 0))
    marks1 = int(request.POST.get('marks1', 0))
    marks2 = int(request.POST.get('marks2', 0))
    marks3 = int(request.POST.get('marks3', 0))
    marks4 = int(request.POST.get('marks4', 0))
    marks5 = int(request.POST.get('marks5', 0))
    question_range = range(1, 11)
    return render(request, 'intermediate2.html', {
        'nq1': nq1,
        'nq2': nq2,
        'nq3': nq3,
        'nq4': nq4,
        'nq5': nq5,
        'marks1': marks1,
        'marks2': marks2,
        'marks3': marks3,
        'marks4': marks4,
        'marks5': marks5,
        'question_range': question_range,
        'max_marks': max_marks,
    })


class GeneratePdf(View):
    def get(self, request, *args, **kwargs):
        print('GeneratePaperRequest2')
        tmarks = 0
        dat = datetime.date.today().strftime("%d/%m/%Y")

        # Fetching parameters from the request
        max_marks_input = request.GET.get('max_marks_input', '')
        Year = int(request.GET.get('Year', 0))
        sub = request.GET.get('subject', '')
        ayear = request.GET.get('ayear', '')
        dep = request.GET.get('dep', '')
        test_name = request.GET.get('test_name', '')
        term = request.GET.get('term', '')
        subcode = request.GET.get('subcode', '')
        div = request.GET.get('div', '')
        ttime = request.GET.get('ttime', '')

        # Fetch hours and minutes from GET
        hours = request.GET.get('hours', '0')
        minutes = request.GET.get('minutes', '0')

        try:
            hours = int(hours)
            minutes = int(minutes)
        except ValueError:
            hours = 0
            minutes = 0

        if hours == 0 and minutes == 0:
            ttime = ""
        elif hours == 0:
            ttime = f"{minutes} minutes"
        elif minutes == 0:
            ttime = f"{hours} hours"
        else:
            ttime = f"{hours} hours {minutes} minutes"

        # Labels up to 10 (a) to (j))
        my_list = ['a)', 'b)', 'c)', 'd)', 'e)', 'f)', 'g)', 'h)', 'i)', 'j)']
        global_selected_questions = []

        # Initialize question lists and counters for each section
        fquestions = {i: [] for i in range(1, 6)}
        qno = {i: 0 for i in range(1, 6)}

        # Dynamically generate question input keys: m1_1 to m5_10, q1_1 to q5_10
        question_inputs = [
            (f"m{section}_{q}", f"q{section}_{q}")
            for section in range(1, 6)
            for q in range(1, 11)
        ]

        for idx, (marks_key, unit_key) in enumerate(question_inputs, start=1):
            marks = request.GET.get(marks_key)
            unit = request.GET.get(unit_key)

            if marks and unit and marks.isdigit() and unit.isdigit():
                a = questionBank.objects.filter(
                    year=Year, subname=sub, unit=int(unit), marks=int(marks)
                )

                if not a.exists():
                    print(f"No questions found for {marks_key}, {unit_key}")
                    continue

                # Select a random question
                try:
                    question = random.choice(list(a))
                except IndexError:
                    continue

                section = (idx - 1) // 10 + 1  # 10 questions per section

                if question not in global_selected_questions:
                    label = my_list[qno[section]] if qno[section] < len(my_list) else f"{chr(97 + qno[section])})"
                    fquestions[section].append((question, label))
                    global_selected_questions.append(question)

                    tmarks += int(marks)
                    qno[section] += 1

        # --- Add 3 extra questions for each main (if available) ---
        for section in range(1, 6):
            current_count = len(fquestions[section])
            extra_needed = 3
            # Try to fetch extra questions for the same marks/unit as inputted
            for i in range(current_count):
                if extra_needed <= 0:
                    break
                orig_question, _ = fquestions[section][i]
                # Find more questions with same year, subname, unit, marks, not already selected
                extra_qs = questionBank.objects.filter(
                    year=Year,
                    subname=sub,
                    unit=orig_question.unit,
                    marks=orig_question.marks
                ).exclude(id__in=[q.id for q, _ in fquestions[section]] + [q.id for q in global_selected_questions])
                for eq in extra_qs:
                    if extra_needed <= 0:
                        break
                    label = my_list[qno[section]] if qno[section] < len(my_list) else f"{chr(97 + qno[section])})"
                    fquestions[section].append((eq, label))
                    global_selected_questions.append(eq)
                    qno[section] += 1
                    extra_needed -= 1

        nq1 = int(request.GET.get('nq1', 0))
        nq2 = int(request.GET.get('nq2', 0))
        nq3 = int(request.GET.get('nq3', 0))
        nq4 = int(request.GET.get('nq4', 0))
        nq5 = int(request.GET.get('nq5', 0))

        marks1 = int(request.GET.get('marks1', 0))
        marks2 = int(request.GET.get('marks2', 0))
        marks3 = int(request.GET.get('marks3', 0))
        marks4 = int(request.GET.get('marks4', 0))
        marks5 = int(request.GET.get('marks5', 0))

        main1_total = nq1 * marks1
        main2_total = nq2 * marks2
        main3_total = nq3 * marks3
        main4_total = nq4 * marks4
        main5_total = nq5 * marks5

        # --- Only show error if the sum of all sections exceeds max_marks ---
        try:
            max_marks_val = int(max_marks_input)
        except Exception:
            max_marks_val = 0

        total_marks = main1_total + main2_total + main3_total + main4_total + main5_total
        if total_marks != max_marks_val:
            return render(request, 'intermediate2.html', {
                'popup_message': f"Total marks of all sections ({total_marks}) exceed maximum marks ({max_marks_val})."
            })

        year_ = {1: 'First Exam', 2: 'Second Exam', 3: 'Third Exam'}.get(Year, 'BE')

        data = {
            "sub": sub,
            "date": dat,
            "ayear": ayear,
            "dep": dep,
            "test_name": test_name,
            "term": term,
            "tmarks": tmarks,
            "subcode": subcode,
            "div": div,
            "ttime": ttime,
            "year": year_,
            "fquestions1": fquestions[1],
            "fquestions2": fquestions[2],
            "fquestions3": fquestions[3],
            "fquestions4": fquestions[4],
            "fquestions5": fquestions[5],
            "max_marks_input": max_marks_input,
            "nq1": nq1,
            "nq2": nq2,
            "nq3": nq3,
            "nq4": nq4,
            "nq5": nq5,
            "marks1": marks1,
            "marks2": marks2,
            "marks3": marks3,
            "marks4": marks4,
            "marks5": marks5,
            "main1_total": main1_total,
            "main2_total": main2_total,
            "main3_total": main3_total,
            "main4_total": main4_total,
            "main5_total": main5_total,
        }

        print("Data passed to template:", data)

        pdf = render_to_pdf('pdf/invoice.html', data)
        if pdf:
            return HttpResponse(pdf, content_type='application/pdf')
        else:
            return HttpResponse("Error generating PDF", status=500)


@login_required(login_url='login')
def index(request):
    return render(request, 'index.html')


@login_required(login_url='login')
def generatePaper(request):
    print('GeneratePaperRequest')
    return render(request, 'generatePaper.html')


@login_required(login_url='login')
def delete(request):
    return render(request, 'delete.html')


@login_required(login_url='login')
def deleteQuestion(request):
    year = request.POST.get('year')
    subname = request.POST.get('subname')
    unit = request.POST.get('unit')

    # Validate input data
    if not year and not subname and not unit:
        a = questionBank.objects.all()
    elif not year and not subname:
        a = questionBank.objects.filter(unit=unit)
    elif not subname and not unit:
        a = questionBank.objects.filter(year=year)
    elif not year and not unit:
        a = questionBank.objects.filter(subname=subname)
    elif not unit:
        a = questionBank.objects.filter(year=year, subname=subname)
    elif not year:
        a = questionBank.objects.filter(subname=subname, unit=unit)
    else:
        a = questionBank.objects.filter(year=year, subname=subname, unit=unit)  # Properly indented

    print(a)
    params = {"a": a, "subname": subname, "year": year, "unit": unit}
    return render(request, 'deleteQuestion.html', params)


@login_required(login_url='login')
def deleteSuccess(request):
    if request.method == "POST":
        ids = request.POST.getlist('ids_to_delete')
        if ids:
            for id in ids:
                try:
                    questionBank.objects.get(id=id).delete()
                except questionBank.DoesNotExist:
                    pass
            messages.success(request, "Selected questions deleted successfully.")
        else:
            messages.warning(request, "No questions selected for deletion.")
        return redirect('deleteQuestion')
    return redirect('deleteQuestion')


@login_required(login_url='login')
def add_question(request):
    if request.method == "POST":
        year = request.POST.get('year')
        subject = request.POST.get('subject')
        subject_code = request.POST.get('subject_code')
        unit = request.POST.get('unit')
        marks = request.POST.get('marks')
        question_text = request.POST.get('question')

        # Validate input data
        if not (year and subject and subject_code and unit and marks and question_text):
            messages.error(request, "All fields are required!")
            return redirect('add_question')

        try:
            # Save the question to the database
            question = questionBank(
                question=question_text,
                subject_code=subject_code,
                marks=marks,
                unit=unit,
                year=year,
                subname=subject
            )
            question.save()

            # Add a success message
            messages.success(request, "Question saved successfully!")
        except Exception as e:
            # Handle any unexpected errors
            messages.error(request, f"An error occurred: {str(e)}")

        # Redirect back to the add_question page
        return redirect('add_question')

    return render(request, 'add_question.html')


def view_questions(request):
    subject = request.GET.get('subject')
    year = request.GET.get('year')
    questions = questionBank.objects.all()
    if subject:
        questions = questions.filter(subname=subject)
    if year:
        questions = questions.filter(year=year)
    return render(request, 'view_questions.html', {'questions': questions})


def edit_question(request, id):
    question = get_object_or_404(questionBank, id=id)
    if request.method == "POST":
        question.question = request.POST.get('question')
        question.subject_code = request.POST.get('subject_code')
        question.marks = request.POST.get('marks')
        question.year = request.POST.get('year')
        question.unit = request.POST.get('unit')
        question.subname = request.POST.get('subject')  # or question.subject = ... if your model uses 'subject'
        question.save()
        messages.success(request, "Question updated successfully!")
        return redirect('view_questions')
    # For GET request, render the edit form
    return render(request, 'edit_question.html', {'question': question})


def deletequestions(request):
    subject = request.GET.get('subject')
    year = request.GET.get('year')
    unit = request.GET.get('unit')
    questions = questionBank.objects.all()
    if subject:
        questions = questions.filter(subname=subject)
    if year:
        questions = questions.filter(year=year)
    if unit:
        questions = questions.filter(unit=unit)

    if request.method == "POST":
        question_id = request.POST.get('question_id')
        if question_id:
            questionBank.objects.filter(id=question_id).delete()
            messages.success(request, "Successfully deleted!")
        # Redirect to the same page with filters preserved
        params = f"?subject={subject}&year={year}&unit={unit}"
        return redirect(f"{request.path}{params}")

    return render(request, 'deletequestions.html', {'questions': questions})
