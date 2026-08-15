# סביבת עבודה של Java 21
FROM eclipse-temurin:21-jdk

WORKDIR /app

# העתקת קוד המקור ותיקיית הספריות לתוך המכולה
COPY src ./src
COPY lib ./lib

# קימפול קבצי ה-Java תוך כדי חיבור לספריות ה-JAR
RUN mkdir bin && javac -cp "lib/*" -d bin $(find src -name "*.java")

# פתיחת הפורט של השרת
EXPOSE 8080

# הרצת השרת (שים לב לשימוש בנקודתיים בין הנתיבים בלינוקס, ולשם החבילה Management)
CMD ["java", "-cp", "bin:lib/*", "Management.Main"]