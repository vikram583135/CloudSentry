from django.urls import path
from . import views

urlpatterns = [
    path('', views.home, name='home'),
    path('login/', views.user_login, name='login'),
    path('logout/', views.user_logout, name='logout'),

    path('student/dashboard/', views.student_dashboard_view, name='student_dashboard'),
    path('student/profile/', views.student_profile_view, name='student_profile'),
    path('student/upload-resume/', views.upload_resume_view, name='upload_resume'),

    path('admin/dashboard/', views.admin_dashboard_view, name='admin_dashboard'),
    path('company/dashboard/', views.company_dashboard_view, name='company_dashboard'),

    path('register/student/', views.register_student, name='register_student'),
    path('register/company/', views.register_company, name='register_company'),
    path('register/admin/', views.register_admin, name='register_admin'),
]
