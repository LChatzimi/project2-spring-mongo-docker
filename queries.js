// 1. Find the total number of reports per ‘Crm Cd” that occurred within a specified time range and sort them in a descending order.
db.crime_reports.aggregate([
    {
        $unwind: "$crimeCodes"
    },
    {
        $match: {
            dateOccurred: {
                $gte: ISODate("2020-01-01T00:00:00Z"),
                $lte: ISODate("2020-12-31T23:59:59Z")
            },
            "crimeCodes.crimeCode": { $ne: "" }
        }
    },
    {
        $group: {
            _id: "$crimeCodes.crimeCode",
            totalReports: { $sum: 1 }
        }
    },
    {
        $sort: { totalReports: -1 }
    }
]);

// 2. Find the total number of reports per day for a specific “Crm Cd” and time range.
db.crime_reports.aggregate([
    {
        $match: {
            "crimeCodes.crimeCode": "510",  // Specify Crime Code
            dateOccurred: {
                $gte: ISODate("2020-01-01T00:00:00Z"),
                $lte: ISODate("2020-12-31T23:59:59Z")
            }
        }
    },
    {
        $group: {
            _id: { $dateToString: { format: "%Y-%m-%d", date: "$dateOccurred" } },
            totalReports: { $sum: 1 }
        }
    },
    {
        $sort: { _id: 1 }
    }
]);


// 3. Find the three most common crimes committed –regardless of code 1, 2, 3, and 4– per area for a specific day.
db.crime_reports.aggregate([
    {
        $match: {
            dateOccurred: {
                $gte: ISODate("2020-01-01T00:00:00Z"), //start of day x
                $lte: ISODate("2020-01-01T23:59:59Z") //end of day x
            },
        }
    },
    {
        $unwind: "$crimeCodes"
    },
    {
        $match: {
            "crimeCodes.crimeCode": {$ne: ""}
        }
    },
    {
        $group: {
            _id: {
                areaName: "$areaInfo.areaName",
                crimeCode: "$crimeCodes.crimeCode"
            },
            totalReports: {$sum: 1}
        }
    },
    {
        $sort: {"totalReports": -1}
    },
    {
        $group: {
            _id: "$_id.areaName",
            topCrimes: {$push: {crime: "$_id.crimeCode", count: "$totalReports"}}
        }
    },
    {
        $project: {
            topCrimes: {$slice: ["$topCrimes", 3]}
        }
    }
]);


//4. Find the two least common crimes committed with regards to a given time range.
db.crime_reports.aggregate([
    {
        $match: {
            dateOccurred: {
                $gte: ISODate("2020-01-01T00:00:00Z"),
                $lte: ISODate("2020-12-31T23:59:59Z")
            }
        }
    },
    {
        $unwind: "$crimeCodes"
    },
    {
        $group: {
            _id: "$crimeCodes.crimeDescription",
            totalReports: { $sum: 1 }
        }
    },
    {
        $sort: { totalReports: 1 }
    },
    {
        $limit: 2
    }
]);


// 5. Find the types of weapon that have been used for the same crime “Crm Cd” in more than one areas
db.crime_reports.aggregate([
    {
        $unwind: "$crimeCodes"
    },
    {
        $match: {
            "crimeCodes.crimeCode": { $ne: "" },
            "weaponInfo.weaponDescription": { $ne: "" }
        }
    },
    {
        $group: {
            _id: {
                crimeCode: "$crimeCodes.crimeCode",
                weaponUsed: "$weaponInfo.weaponDescription"
            },
            areas: { $addToSet: "$areaInfo.areaName" }
        }
    },
    {
        $match: {
            "areas.1": { $exists: true }
        }
    },
    {
        $group: {
            _id: "$_id.crimeCode",
            weapons: { $addToSet: "$_id.weaponUsed" }
        }
    },
    {
        $project: {
            _id: 0,
            crimeCode: "$_id",
            weapons: 1
        }
    }
]);


// 6. Find the fifty most upvoted reports for a specific day.
db.crime_reports.aggregate([
    {
        $match: {
            dateOccurred: {
                $gte: ISODate("2020-01-01T00:00:00Z"), //start of day x
                $lte: ISODate("2020-01-01T23:59:59Z") //end of day x
            }
        }
    },
    {
        $project: {
            drNo: 1,
            dateOccurred: 1,
            totalUpvotes: { $size: { $ifNull: ["$upvotes", []] } }
        }
    },
    {
        $sort: { totalUpvotes: -1 }
    },
    {
        $limit: 50
    }
]);

// 7. Find the fifty most active police officers, with regard to the total number of upvotes.
db.crime_reports.aggregate([
    {
        $unwind: "$upvotes"
    },
    {
        $group: {
            _id: {
                officerName: "$upvotes.officerName",
                officerEmail: "$upvotes.officerEmail",
                badgeNumber: "$upvotes.badgeNumber"
            },
            totalUpvotes: { $sum: 1 }
        }
    },
    {
        $sort: { totalUpvotes: -1 }
    },
    {
        $limit: 50
    },
    {
        $project: {
            officerName: "$_id.officerName",
            officerEmail: "$_id.officerEmail",
            badgeNumber: "$_id.badgeNumber",
            totalUpvotes: 1
        }
    }
]);

// 8. Find the top fifty police officers, with regard to the total number of areas for which they have upvoted reports.
db.crime_reports.aggregate([
    {
        $unwind: "$upvotes"
    },
    {
        $group: {
            _id: {
                officerName: "$upvotes.officerName",
                officerEmail: "$upvotes.officerEmail",
                badgeNumber: "$upvotes.badgeNumber"
            },
            areas: { $addToSet: "$areaInfo.areaName" }
        }
    },
    {
        $project: {
            officerName: "$_id.officerName",
            officerEmail: "$_id.officerEmail",
            badgeNumber: "$_id.badgeNumber",
            totalAreas: { $size: "$areas" }
        }
    },
    {
        $sort: { totalAreas: -1 }
    },
    {
        $limit: 50
    }
]);


// 9. Find all reports for which the same e-mail has been used for more than one badge numbers when casting an upvote.
//find a report and change the badge number in order to test the query, for example:
db.crime_reports.updateOne(
    {
        drNo: "200907217",
        "upvotes.badgeNumber": "000000241"
    },
    {
        $set: { "upvotes.$.badgeNumber": "000000240" }
    }
);

db.crime_reports.aggregate([
    {
        $unwind: "$upvotes"
    },
    {
        $group: {
            _id: "$upvotes.officerEmail",
            badgeNumbers: { $addToSet: "$upvotes.badgeNumber" },
            reports: { $addToSet: "$drNo" }
        }
    },
    {
        $match: {
            "badgeNumbers.1": { $exists: true }
        }
    },
    {
        $project: {
            officerEmail: "$_id",
            badgeNumbers: 1,
            reports: 1
        }
    }
], { allowDiskUse: true });


// 10. Find all areas for which a given name has casted a vote for a report involving it.
db.crime_reports.aggregate([
    {
        $unwind: "$upvotes"
    },
    {
        $match: {
            "upvotes.officerName": "Officer 87"
        }
    },
    {
        $group: {
            _id: "$upvotes.officerName",
            areas: { $addToSet: "$areaInfo.areaName" },
            reports: { $addToSet: "$drNo" }
        }
    },
    {
        $project: {
            officerName: "$_id",
            areas: 1,
            reports: 1
        }
    }
]);