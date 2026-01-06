from django.contrib import admin
from .models import StudentProfile, CompanyProfile, JobPosting, InterviewSchedule

admin.site.register(StudentProfile)
admin.site.register(CompanyProfile)
admin.site.register(JobPosting)

admin.site.register(InterviewSchedule)
