package customcard;
import java.util.ArrayList;
import java.util.List;

public class CustomCard {

    private String studentName;
    private long studentId;
    private int studentLevel;
    private List<Double> grades = new ArrayList<Double>();
    private List<String> subjects = new ArrayList<String>();

    public CustomCard(String studentName, long studentId, int studentLevel) {
        this.studentName = studentName;
        this.studentId = studentId;
        this.studentLevel = studentLevel;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public long getStudentId() {
        return studentId;
    }

    public int getStudentLevel() {
        return studentLevel;
    }

    public void setStudentLevel(int studentLevel) {
        this.studentLevel = studentLevel;
    }

    public List<Double> getGrades() {
        return grades;
    }

    public List<String> getSubjects() {
        return subjects;
    }

    public void addSubject(String subjectName, double subjectGrade) {
        // Add the subject and the corresponding grade
        this.subjects.add(subjectName.toUpperCase());
        this.grades.add(subjectGrade);
    }

    public double getGradeBySubjectName(String subjectName) {
        int index = -1;
        String sub = subjectName.toUpperCase();
        for (String subject : this.subjects) {
            if (subject.equals(sub)) {
                index++;
                break;
            }
        }

        if (index >= 0)
            return this.grades.get(index);

        return -1;  // the subject does not exist
    }

    public boolean setGradeBySubjectName(String subjectName, double grade) {
        int index = -1;
        String sub = subjectName.toUpperCase();
        for (String subject : this.subjects) {
            if (subject.equals(sub)) {
                index++;
                break;
            }
        }

        if (index >= 0) {
            this.grades.set(index, grade);
            return true;
        }

        return false; // the subject does not exist
    }

    @java.lang.Override
    public java.lang.String toString() {
        String subjects_grades = "";
        int index = 0;
        for(String subject : this.subjects){
            if(index > 0){
                subjects_grades += "; ";
            }
            subjects_grades += "(" + subject + ", " + this.grades.get(index) + ")";
            index++;
        }
        return "CustomCard:" +
                "\nStudent name: '" + studentName + '\'' +
                "\nStudent id: " + studentId +
                "\nStudent level: " + studentLevel +
                "\nGrades: " + subjects_grades;
    }
}