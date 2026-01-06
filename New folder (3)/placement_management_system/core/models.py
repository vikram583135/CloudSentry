from django.contrib.auth.models import AbstractUser
from django.db import models
from django.contrib.auth.models import User

class User(AbstractUser):
    is_student = models.BooleanField(default=False)
    is_company = models.BooleanField(default=False)
    is_admin = models.BooleanField(default=False)

class StudentProfile(models.Model):
    user = models.OneToOneField(User, on_delete=models.CASCADE)
    full_name = models.CharField(max_length=100)
    branch = models.CharField(max_length=100)
    cgpa = models.FloatField()
    backlogs = models.IntegerField()
    resume = models.FileField(upload_to='resumes/', null=True, blank=True)
    phone = models.CharField(max_length=15, null=True, blank=True)
    skills = models.TextField(null=True, blank=True)

    def __str__(self):
        return self.user.username
    
class CompanyProfile(models.Model):
    user = models.OneToOneField(User, on_delete=models.CASCADE)
    company_name = models.CharField(max_length=200)
    website = models.URLField()
    description = models.TextField()

    def __str__(self):
        return self.company_name

class JobPosting(models.Model):
    company = models.ForeignKey(CompanyProfile, on_delete=models.CASCADE)
    title = models.CharField(max_length=200)
    description = models.TextField()
    eligible_branches = models.CharField(max_length=255)
    min_cgpa = models.FloatField()
    max_backlogs = models.IntegerField()
    deadline = models.DateField()

    def __str__(self):
        return self.title

class Application(models.Model):
    student = models.ForeignKey(StudentProfile, on_delete=models.CASCADE)
    job = models.ForeignKey(JobPosting, on_delete=models.CASCADE)
    status = models.CharField(max_length=50, choices=[
        ('Applied', 'Applied'),
        ('Shortlisted', 'Shortlisted'),
        ('Interviewed', 'Interviewed'),
        ('Placed', 'Placed'),
        ('Rejected', 'Rejected'),
    ])
    applied_on = models.DateTimeField(auto_now_add=True)

    def __str__(self):
        return f"{self.student.user.username} - {self.job.title}"

class InterviewSchedule(models.Model):
    application = models.OneToOneField(Application, on_delete=models.CASCADE)
    date = models.DateField()
    time = models.TimeField()
    location = models.CharField(max_length=200)

    def __str__(self):
        return f"{self.application.student.user.username} - {self.date}"

class AuditLog(models.Model):
    user = models.ForeignKey(User, on_delete=models.SET_NULL, null=True)
    action = models.CharField(max_length=255)
    timestamp = models.DateTimeField(auto_now_add=True)
