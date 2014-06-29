package jsouptest;

import java.io.File;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class transcript {

	public static void main(String[] args) {
		
		File input = new File("C:\\Users\\Zach\\dev\\jsouptest\\src\\bad_transcript.html");
		Document document;
		try{
			document = Jsoup.parse(input, "UTF-8");
			
		} catch(Exception e){
			document = null;			
		}

        //Extract program, scholarships, total credits, and CGPA
        Elements rows = document.getElementsByClass("fieldmediumtext");

        /*
         * Main loop:
         * This will iterate through every row of the transcript data and check for various tokens
         * Once a match is found, the value in the appropriate row will be saved to a variable
         */
        // row iterates through all of the rows in the transcript searching for tokens
        // dataRow finds the rows containing the data once a token is found
        Element dataRow;

        double cgpa = 0;
        int totalCredits = 0;

        int index = 0;
        for (Element row : rows){
            //Check the text at the start of the row
            //If it matches one of the tokens, take the corresponding data out
            //of one of the following rows, depending on the HTML layout

            //CGPA
            if(row.text().startsWith(Token.CUM_GPA.getString())){
                dataRow = rows.get(index+1);
                try{
                    cgpa = Double.parseDouble(dataRow.text());
                }
                catch (NumberFormatException e){
                    cgpa = -1;
                }
            }
            //Credits
            if(row.text().startsWith(Token.TOTAL_CREDITS.getString())){
                dataRow = rows.get(index+1);
                try{
                    totalCredits = (int)Double.parseDouble(dataRow.text());
                }
                catch (NumberFormatException e){
                    totalCredits = -1;
                }
            }
            //Semester Information
            if(row.text().startsWith(Token.FALL.getString()) ||
                    row.text().startsWith(Token.WINTER.getString()) ||
                    row.text().startsWith(Token.SUMMER.getString())){

                String program = "";
                String bachelor = "";
                int programYear = 99;
                int termCredits = 0;
                double termGPA = 0.0;
                boolean fullTime = false;
                boolean satisfactory = false;
                

                //Search rows until the end of the semester is reached
                //Conditions for end of semester:
                //1. End of transcript is reached
                //2. The words "Fall" "Summer" or "Winter" appear
                int semesterIndex = index +1;
                dataRow = rows.get(semesterIndex);

                while(true){

                    //Student has graduated
                    if(dataRow.text().contains(Token.GRANTED.getString())){
                        break;
                    }

                    //Semester Info
                    else if(dataRow.text().startsWith(Token.BACHELOR.getString()) ||
                            dataRow.text().startsWith(Token.MASTER.getString()) ||
                            dataRow.text().startsWith(Token.DOCTOR.getString())){

                        //Example string:
                        //"Bachelor&nbps;of&nbsp;Engineering"<br>
                        //"Full-time&nbsp;Year&nbsp;0"<br>
                        //"Electrical&nbsp;Engineering"

                        String[] degreeDetails = dataRow.text().split(" ");
                        bachelor = degreeDetails[0];

                        //Check if student is full time
                        if(degreeDetails[1].startsWith("Full-time")){
                            fullTime = true;
                        }

                        else if(degreeDetails[1].contains("0")){
                            programYear = 0;
                        }
                        else if(degreeDetails[1].contains("1")){
                            programYear = 1;
                        }
                        else if(degreeDetails[1].contains("2")){
                            programYear = 2;
                        }
                        else if(degreeDetails[1].contains("3")){
                            programYear = 3;
                        }
                        else if(degreeDetails[1].contains("4")){
                            programYear = 4;
                        }
                        else if(degreeDetails[1].contains("5")){
                            programYear = 5;
                        }

                        program = degreeDetails[2];
                    }
                    //Term GPA
                    else if(dataRow.text().startsWith(Token.TERM_GPA.getString())){
                        termGPA = Double.parseDouble(rows.get(semesterIndex + 1).text());
                    }
                    //Term Credits
                    else if(dataRow.text().startsWith(Token.TERM_CREDITS.getString())){
                        termCredits = (int)Double.parseDouble(rows.get(semesterIndex + 2).text());
                    }

                    //Extract course information if row contains a course code
                    //Regex looks for a string in the form "ABCD ###"
                    else if(dataRow.text().matches("[A-Za-z]{4} [0-9]{3}.*")){
                        String courseCode = "";
                        //One semester courses are in the form ABCD ###
                        if(dataRow.text().matches("[A-Za-z]{4} [0-9]{3}")){
                            courseCode = dataRow.text();
                        }
                        //Multi semester courses are in the form ABCD ###D#
                        else{
                            //Extract first seven characters from string
                            try{
                                courseCode = dataRow.text().substring(0, 10);
                            }
                            catch(Exception e){
                                e.printStackTrace();
                            }
                        }

                        String courseTitle = rows.get(semesterIndex + 2).text();

                        //Failed courses are missing the earned credits row
                        int credits = 0;

                        //Check row to see if earned credit exists
                        try{
                            credits = Integer.parseInt(rows.get(semesterIndex + 6).text());
                        }
                        catch(NumberFormatException e){
                            //Course failed -> Earned credit = 0
                        }
                        catch(IndexOutOfBoundsException e){
                            e.printStackTrace();
                        }

                        //Obtain user's grade
                        String userGrade = rows.get(semesterIndex+4).text();

                        //Check for deferred classes
                        if(userGrade.equals("L")){
                            userGrade = rows.get(semesterIndex + 13).text();
                        }

                        //If average grades haven't been released on minerva, index will be null
                        String averageGrade = "";
                        try{
                            //Regex looks for a letter grade
                            if(rows.get(semesterIndex+7).text().matches("[ABCDF].|[ABCDF]")){
                                averageGrade = rows.get(semesterIndex+7).text();
                            }
                            //Failed course, average grade appears one row earlier
                            else if(rows.get(semesterIndex+6).text().matches("[ABCDF].|[ABCDF]")){
                                averageGrade = rows.get(semesterIndex+6).text();
                            }
                        }
                        catch(IndexOutOfBoundsException e){
                            //String not found
                        }
                        
                        System.out.println(courseCode);
                        
                        
                    }

                    //Extract transfer credit information
                    else if(dataRow.text().startsWith(Token.CREDIT_EXEMPTION.getString())){
                        String courseTitle;
                        String courseCode;
                        String userGrade = "N/A";
                        String averageGrade = "";
                        int credits = 0;

                        //Individual transferred courses not listed
                        if(!rows.get(semesterIndex + 3).text().matches("[A-Za-z]{4}.*")){
                            courseCode = rows.get(semesterIndex + 2).text();

                            //Extract the number of credits granted
                            credits = extractCredits(courseCode);

                            
                        }

                        //Individual transferred courses listed
                        else{
                            //Try checking for the number of credits transferred per course
                            try{
                                courseCode = rows.get(semesterIndex + 2).text();
                                courseTitle = rows.get(semesterIndex + 3).text() + " " + rows.get(semesterIndex+4).text();
                                credits = Integer.parseInt(rows.get(semesterIndex + 5).text());
                                
                            }

                            //Number of credits per course not listed
                            catch(NumberFormatException e){
                                try{
                                    courseCode = rows.get(semesterIndex + 2).text();
                                    courseTitle = "";

                                    credits = extractCredits(courseCode);

                                    //Add the course codes for transferred courses
                                    int addedIndex = 3;
                                    boolean first = true;
                                    while(rows.get(semesterIndex + addedIndex).text().matches("[A-Za-z]{4}.*")){
                                        if(!first){
                                            courseTitle += "\n";
                                        }
                                        courseTitle = courseTitle + rows.get(semesterIndex + addedIndex).text() + " " + rows.get(semesterIndex+addedIndex+1).text();
                                        addedIndex = addedIndex + 2;
                                        first = false;
                                    }

                                    

                                }
                                catch(IndexOutOfBoundsException e2){
                                    e.printStackTrace();
                                }
                                catch(Exception e3){
                                    e.printStackTrace();
                                }
                            }
                        }
                        

                        termCredits = credits;
                    }

                    /**
                     * Breaks the loop if the next semester is reached
                     */
                    if(dataRow.text().startsWith(Token.FALL.getString()) ||
                            dataRow.text().startsWith(Token.WINTER.getString()) ||
                            dataRow.text().startsWith(Token.SUMMER.getString())){
                        break;
                    }

                    semesterIndex++;

                    //Reached the end of the transcript, break loop
                    try{
                        dataRow = rows.get(semesterIndex);
                    }
                    catch(IndexOutOfBoundsException e){
                        break;
                    }
                }
                System.out.println("***********END OF SEMESTER***************");
                System.out.println(program);
                System.out.println(bachelor);
                System.out.println(programYear);
                System.out.println(termCredits);
                System.out.println(termGPA);
            }
            index++;
        }
        

	}
	
	//Extracts the number of credits
    private static int extractCredits(String creditString){
        int numCredits;

        try{
            creditString = creditString.replaceAll("\\s", "");
            String[] creditArray = creditString.split("-");
            creditArray = creditArray[1].split("credits");
            numCredits = Integer.parseInt(creditArray[0]);
            return numCredits;
        }
        catch (NumberFormatException e){
            return 99;
        }
        catch(Exception e){
            return 88;
        }
    }

}
