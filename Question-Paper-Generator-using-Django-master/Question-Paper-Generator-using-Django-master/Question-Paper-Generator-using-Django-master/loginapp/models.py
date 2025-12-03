from django.db import models

class questionBank(models.Model):
    question = models.CharField(max_length=300)
    subject_code = models.CharField(max_length=100)
    marks = models.IntegerField()
    unit = models.IntegerField()
    year = models.IntegerField()
    subname = models.CharField(max_length=100)

    def __str__(self):
        return self.question

    class Meta:
        db_table = "questionBank"