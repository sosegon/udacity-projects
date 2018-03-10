package customcard;

public class Main {
    public static void main(String[] args) {
        CustomCard card = new CustomCard("Sebastian Velasquez", 765234, 7);
        card.addSubject("Math", 9.0);
        card.addSubject("Geometry", 7.8);
        card.setGradeBySubjectName("math", 7.9);
        System.out.println(card.getGradeBySubjectName("math"));

        System.out.println(card.toString());
    }
}