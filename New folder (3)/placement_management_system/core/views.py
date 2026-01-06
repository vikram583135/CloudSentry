from django.shortcuts import render, redirect
from django.contrib.auth import authenticate, login, logout
from django.contrib.auth.decorators import login_required, user_passes_test
from django.contrib import messages
from .models import StudentProfile, InterviewSchedule
from .forms import StudentProfileForm

# Role check functions
def is_student(user): return hasattr(user, 'studentprofile')
def is_company(user): return hasattr(user, 'companyprofile')
def is_admin(user): return user.is_superuser or user.is_staff

# ------------------------
# Login View
def user_login(request):
    if request.method == 'POST':
        username = request.POST.get('username')
        password = request.POST.get('password')
        user = authenticate(request, username=username, password=password)

        if user:
            login(request, user)
            if is_admin(user):
                return redirect('admin_dashboard')
            elif is_student(user):
                return redirect('student_dashboard')
            elif is_company(user):
                return redirect('company_dashboard')
            else:
                return redirect('home')
        else:
            messages.error(request, "Invalid credentials")
    return render(request, 'registration/login.html')

# ------------------------
# Student Views

@login_required
@user_passes_test(is_student)
def student_dashboard_view(request):
    student = request.user
    applied_count = JobApplication.objects.filter(student=student).count()
    interviews_count = InterviewSchedule.objects.filter(student=student).count()
    placement_status = "Placed" if student.studentprofile.placement_status else "Not Placed"

    return render(request, 'student/student_dashboard.html', {
        'applied_count': applied_count,
        'interviews_count': interviews_count,
        'placement_status': placement_status
    })

@login_required
@user_passes_test(is_student)
def student_profile_view(request):
    profile, _ = StudentProfile.objects.get_or_create(user=request.user)
    if request.method == 'POST':
        form = StudentProfileForm(request.POST, instance=profile)
        if form.is_valid():
            form.save()
            messages.success(request, "Profile updated.")
            return redirect('student_profile')
    else:
        form = StudentProfileForm(instance=profile)
    return render(request, 'student/student_profile.html', {'form': form})

@login_required
@user_passes_test(is_student)
def upload_resume_view(request):
    profile = request.user.studentprofile
    if request.method == 'POST':
        form = ResumeUploadForm(request.POST, request.FILES, instance=profile)
        if form.is_valid():
            form.save()
            messages.success(request, "Resume uploaded.")
            return redirect('upload_resume')
    else:
        form = ResumeUploadForm(instance=profile)
    return render(request, 'student/upload_resume.html', {'form': form})
