# RekoPot - License Plate Recognition Service

### Introduction
Project 2: Rekopot</br>
University Name: http://www.sjsu.edu/</br>
Course: [Cloud Technologies](http://info.sjsu.edu/web-dbgen/catalog/courses/CMPE281.html)</br>
Professor: [Sanjay Garje](https://www.linkedin.com/in/sanjaygarje/)</br>
ISA: [Anushri Srinath Aithal ](https://www.linkedin.com/in/anushri-aithal/)</br>
Students: Yunting Xiang, Junteng Tan, Sangwon Song, Kevin Lai</br>

### Rekopot Introduction
This project aims to create a 3 tier web application which is able to provide highly available, highly scalable license plate storage and recognition service. The AWS cloud services are utilized to accelerate the development, build, test and deployment of this project. The following functions are implemented:

1. User registration
2. User login/logout
3. Facebook login integration
4. Upload/delete/list license plate images
5. Different handling to plate images and non-plate images
6. Plate number extraction, Plate state name extraction
7. Detected tags modification/saving/cancellation 
8. Admin management

### Feature List
1.  Sign up a new user and create the corresponding account in AWS RDS MySQL database if it does not exist. 
2.  Verify if the username/password credential is valid when a user tries to log in.
3.  Allow the authorized user to upload license plate images.  The fields like User’s first name, User’s last name, image creation time will be recorded. The maximum file size is 10MB.
4.  List all the files which have been uploaded by the authorized user.
5.  For each existing image, automatically display this image and provide the capability for the user to extract plate number/state name, modify/save the detected tags, delete the image.
6.  Based on user's request, save the images which are not detected as license plate
7.  Provide a Admin role which can list all the users’ images and delete them if needed. 
8.  User logout.


### The adopted technologies:
1. AWS services:
R53, EC2, Rekognition, RDS, S3, S3 Transfer Acceleration, Glacier, CloudFront,  AutoScaling Group, ELB,  CodePipeline, CodeCommit, CodeBuild, Beanstalk, CloudWatch, SNS

2. Development tools/library:
Spring Boot, AWS toolkit for Eclipse, Maven, Hibernate, JQuery, Thymeleaf


### Pre-requisites Set Up
1. AWS services
In your AWS cloud account, please configure the following services as the smallest set to run this project:
R53, EC2, S3 bucket, RDS, CloudFront

2. Required softwares to download
Spring Boot, Eclipse(With AWS toolkit), JDK1.8, Maven 
Other softwares/plugins will be downloaded by Maven based on the dependency file pom.xml.

### How to set up and run locally
1. Install maven, jdk8, git

$ sudo yum install -y maven

$ sudo yum install -y java-1.8.0-openjdk-headless.x86_64

$ sudo yum install -y git

2. Git clone

$ git clone https://github.com/ytxiang/Rekopot.git

3. Modify application.yaml and create a keystore file

$ keytool -genkey
    -keystore keystore.p12
    -storetype PKCS12 
    -keyalg RSA 
    -storepass <password> 
    -validity 730 
    -keysize 2048 
  
4. Compile and make a package

$ mvn package

5. Launch the RekoPot application

$ sudo java -jar ./RekoPot-0.0.1.jar

### Sample Demo Screenshots

- New User Registration Page
![Fig.1 User signup Form](https://raw.githubusercontent.com/ytxiang/Rekopot/master/images/signup1.png)


- User Signup Success Page
![Fig.2 User signed up](https://raw.githubusercontent.com/ytxiang/Rekopot/master/images/signup2.png)


- User Login Page
![Fig.3 Login Page](https://raw.githubusercontent.com/ytxiang/Rekopot/master/images/userlogin.png)


- Facebook Login Page
![Fig.4 Facebook Login Page](https://raw.githubusercontent.com/ytxiang/Rekopot/master/images/facebooklogin.png)


- Upload/Extract/List Images Page

![Fig.5 Upload/Extract/List Images Page](https://raw.githubusercontent.com/ytxiang/Rekopot/master/images/extract.png)


- Non Plate Images Error Handling Page
![Fig.6 Non Plate Images Error Handling Page](https://raw.githubusercontent.com/ytxiang/Rekopot/master/images/nonplateimages.png)


- Delete Images Page
![Fig.7 Delete Images Page](https://raw.githubusercontent.com/ytxiang/Rekopot/master/images/deleteimages.png)


- Save Non Plate Images Page
![Fig.8 Save Non Plate Images Page](https://raw.githubusercontent.com/ytxiang/Rekopot/master/images/savenonplate.png)


- Update Detected Tags Page
![Fig.9 Update Detected Tags Page](https://raw.githubusercontent.com/ytxiang/Rekopot/master/images/updatetags.png)


- Admin List Images Page
![Fig.10 Admin List Images Page](https://raw.githubusercontent.com/ytxiang/Rekopot/master/images/adminlistfiles.png)


- Admin Search Users' Images Page
![Fig.11 Admin Search Users' Images Page](https://raw.githubusercontent.com/ytxiang/Rekopot/master/images/adminsearchuser.png)


- Admin Delete Users' Images Page
![Fig.12 Admin Delete Users' Images Page](https://raw.githubusercontent.com/ytxiang/Rekopot/master/images/admindeletefile.png)

