## Prerequisites 
- Java
- [Spring Boot](https://spring.io/projects/spring-boot)
- [Maven](https://maven.apache.org/guides/index.html)
- [Mongo DB](https://docs.mongodb.com/guides/)
- [Lombok](https://objectcomputing.com/resources/publications/sett/january-2010-reducing-boilerplate-code-with-project-lombok)
- [Docker Compose](https://docs.docker.com/compose/)


## Tools
- Eclipse or IntelliJ IDEA (or any preferred IDE) 
- Maven 
- Postman (or any RESTful API testing tool)


<br/>


###  Build application
**cd /absolute-path-to-directory/project2-spring-mongo-docker**  
and try below command in terminal
> **```mvn clean install```** it will build application and create **jar** file under target directory 


#

###  Run db
**cd /absolute-path-to-directory/project2-spring-mongo-docker**  
and try below command in terminal
> **``` docker compose up mongo -d ```** it will run mongo db container in background

#

###  Run application
**cd /absolute-path-to-directory/project2-spring-mongo-docker**  
and try below command in terminal
> **``` docker compose up crime-reports-service --build -d ```** it will run tomcat container in background and deploy application

#
    
### API Endpoints Call Examples

- Endpoints that implement requested queries are as follows:
    > **GET Mapping** http://localhost:8088/api/crime-reports/query1?startDate=2020-01-01&endDate=2020-12-31 - Query1
    
    > **GET Mapping** http://localhost:8088/api/crime-reports/query2?crimeCode=510&startDate=2020-01-01&endDate=2020-12-31 - Query2

    > **GET Mapping** http://localhost:8088/api/crime-reports/query3?date=2020-01-01 - Query3
    
    > **GET Mapping** http://localhost:8088/api/crime-reports/query4?startDate=2020-01-01&endDate=2020-12-31 - Query4
   
    > **GET Mapping** http://localhost:8088/api/crime-reports/query5 - Query5
   
    > **GET Mapping** http://localhost:8088/api/crime-reports/query6?date=2020-01-01 - Query6
    
    > **GET Mapping** http://localhost:8088/api/crime-reports/query7 - Query7
  
    


- #### Add Upvote to Crime Report
    
    > **POST Mapping** http://localhost:8088/api/crime-reports/190326475/upvote  - Add upvote to crime report 
                                                           
    Request Body  
    ```
   {
      "officerName" : "user123",
      "officerEmail" : "user123@mail.com",
      "badgeNumber" : "972384783"
   }
    ``` 
