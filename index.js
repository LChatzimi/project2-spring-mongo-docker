db.crime_reports.createIndex({ "crimeCodes.crimeCode": 1, "dateOccurred": 1 });
db.crime_reports.createIndex({ "dateOccurred": 1, "areaInfo.areaName": 1, "crimeCodes.crimeCode": 1 });
db.crime_reports.createIndex({ "crimeCodes.crimeDescription": 1, "dateOccurred": 1 });
db.crime_reports.createIndex({ "crimeCodes.crimeCode": 1, "weaponInfo.weaponDescription": 1, "areaInfo.areaName": 1 });
db.crime_reports.createIndex({ "dateOccurred": 1, "upvotes": 1 });
db.crime_reports.createIndex({ "upvotes.officerEmail": 1, "upvotes.badgeNumber": 1 });
db.crime_reports.createIndex({ "upvotes.officerName": 1, "areaInfo.areaName": 1 });