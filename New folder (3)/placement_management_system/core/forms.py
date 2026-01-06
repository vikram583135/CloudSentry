from django import forms
from django.contrib.auth.forms import UserCreationForm
from .models import User, StudentProfile, CompanyProfile

class StudentSignUpForm(UserCreationForm):
    cgpa = forms.FloatField()
    backlogs = forms.IntegerField()
    branch = forms.CharField(max_length=100)

    class Meta:
        model = User
        fields = ['username', 'email', 'password1', 'password2']

    def save(self, commit=True):
        user = super().save(commit=False)
        user.is_student = True
        if commit:
            user.save()
            StudentProfile.objects.create(
                user=user,
                cgpa=self.cleaned_data['cgpa'],
                backlogs=self.cleaned_data['backlogs'],
                branch=self.cleaned_data['branch'],
            )
        return user

class CompanySignUpForm(UserCreationForm):
    company_name = forms.CharField(max_length=200)
    website = forms.URLField()
    description = forms.CharField(widget=forms.Textarea)

    class Meta:
        model = User
        fields = ['username', 'email', 'password1', 'password2']

    def save(self, commit=True):
        user = super().save(commit=False)
        user.is_company = True
        if commit:
            user.save()
            CompanyProfile.objects.create(
                user=user,
                company_name=self.cleaned_data['company_name'],
                website=self.cleaned_data['website'],
                description=self.cleaned_data['description'],
            )
        return user

class AdminSignUpForm(UserCreationForm):
    class Meta:
        model = User
        fields = ['username', 'email', 'password1', 'password2']

    def save(self, commit=True):
        user = super().save(commit=False)
        user.is_admin = True
        if commit:
            user.save()
        return user

class StudentProfileForm(forms.ModelForm):
    class Meta:
        model = StudentProfile
        fields = ['full_name', 'branch', 'cgpa', 'backlogs', 'phone', 'skills']