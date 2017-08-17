import com.sforce.async.*;
import com.sforce.soap.enterprise.EnterpriseConnection;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;

import java.io.*;
import java.util.*;


public class BulkExample {


    public static void main(String[] args)
            throws AsyncApiException, ConnectionException, IOException {
        BulkExample example = new BulkExample();
        // Replace arguments below with your credentials and test file name
        // The first parameter indicates that we are loading Account records
        example.runSample("Account", Creds.getUsrName(), Creds.getPassWrd(), "mySampleData.csv");
    }

    /**
     * Creates a Bulk API job and uploads batches for a CSV file.
     */
    public void runSample(String sobjectType, String userName, String password, String sampleFileName) throws AsyncApiException, ConnectionException, IOException {
        System.out.println("RUNNING THE SAMPLE...");
        BulkConnection connection = getBulkConnection(userName, password);


        doBulkQuery(connection);



        /*
        JobInfo job = createJob(sobjectType, connection);
        List<BatchInfo> batchInfoList = createBatchesFromCSVFile(connection, job, sampleFileName);
        closeJob(connection, job.getId());
        awaitCompletion(connection, job, batchInfoList);
        checkResults(connection, job, batchInfoList);
        */
    }

    /**
     * Gets the results of the operation and checks for errors.
     */
    private void checkResults(BulkConnection connection, JobInfo job, List<BatchInfo> batchInfoList)
            throws AsyncApiException, IOException {
        // batchInfoList was populated when batches were created and submitted
        for (BatchInfo b : batchInfoList) {
            CSVReader rdr =
                    new CSVReader(connection.getBatchResultStream(job.getId(), b.getId()));
            List<String> resultHeader = rdr.nextRecord();
            int resultCols = resultHeader.size();

            List<String> row;
            while ((row = rdr.nextRecord()) != null) {
                Map<String, String> resultInfo = new HashMap<String, String>();
                for (int i = 0; i < resultCols; i++) {
                    resultInfo.put(resultHeader.get(i), row.get(i));
                }
                boolean success = Boolean.valueOf(resultInfo.get("Success"));
                boolean created = Boolean.valueOf(resultInfo.get("Created"));
                String id = resultInfo.get("Id");
                String error = resultInfo.get("Error");
                if (success && created) {
                    System.out.println("Created row with id " + id);
                } else if (!success) {
                    System.out.println("Failed with error: " + error);
                }
            }
        }
    }



    private void closeJob(BulkConnection connection, String jobId) throws AsyncApiException {
        JobInfo job = new JobInfo();
        job.setId(jobId);
        job.setState(JobStateEnum.Closed);
        connection.updateJob(job);
    }



    /**
     * Wait for a job to complete by polling the Bulk API.
     *
     * @param connection
     *            BulkConnection used to check results.
     * @param job
     *            The job awaiting completion.
     * @param batchInfoList
     *            List of batches for this job.
     * @throws AsyncApiException
     */
    private void awaitCompletion(BulkConnection connection, JobInfo job, List<BatchInfo> batchInfoList) throws AsyncApiException {
        long sleepTime = 0L;
        Set<String> incomplete = new HashSet<String>();
        for (BatchInfo bi : batchInfoList) {
            incomplete.add(bi.getId());
        }
        while (!incomplete.isEmpty()) {
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {}
            System.out.println("Awaiting results..." + incomplete.size());
            sleepTime = 10000L;
            BatchInfo[] statusList =
                    connection.getBatchInfoList(job.getId()).getBatchInfo();
            for (BatchInfo b : statusList) {
                if (b.getState() == BatchStateEnum.Completed
                        || b.getState() == BatchStateEnum.Failed) {
                    if (incomplete.remove(b.getId())) {
                        System.out.println("BATCH STATUS:\n" + b);
                    }
                }
            }
        }
    }


    /**
     * Create a new job using the Bulk API.
     *
     * @param sobjectType
     *            The object type being loaded, such as "Account"
     * @param connection
     *            BulkConnection used to create the new job.
     * @return The JobInfo for the new job.
     * @throws AsyncApiException
     */
    private JobInfo createJob(String sobjectType, BulkConnection connection)
            throws AsyncApiException {
        JobInfo job = new JobInfo();
        job.setObject(sobjectType);
        job.setOperation(OperationEnum.insert);
        job.setContentType(ContentType.CSV);
        job = connection.createJob(job);
        System.out.println(job);
        return job;
    }



    /**
     * Create the BulkConnection used to call Bulk API operations.
     */
    private BulkConnection getBulkConnection(String userName, String password)
            throws ConnectionException, AsyncApiException {
        ConnectorConfig enterpriseConfig = new ConnectorConfig();
        enterpriseConfig.setUsername(userName);
        enterpriseConfig.setPassword(password);
        enterpriseConfig.setAuthEndpoint(Creds.getEndpoint());
        // Creating the connection automatically handles login and stores
        // the session in enterpriseConfig
        new EnterpriseConnection(enterpriseConfig);
        // When PartnerConnection is instantiated, a login is implicitly
        // executed and, if successful,
        // a valid session is stored in the ConnectorConfig instance.
        // Use this key to initialize a BulkConnection:
        ConnectorConfig config = new ConnectorConfig();
        config.setSessionId(enterpriseConfig.getSessionId());
        // The endpoint for the Bulk API service is the same as for the normal
        // SOAP uri until the /Soap/ part. From here it's '/async/versionNumber'
        String soapEndpoint = enterpriseConfig.getServiceEndpoint();
        String apiVersion = "40.0";
        String restEndpoint = soapEndpoint.substring(0, soapEndpoint.indexOf("Soap/")) + "async/" + apiVersion;
        config.setRestEndpoint(restEndpoint);
        // This should only be false when doing debugging.
        config.setCompression(true);
        // Set this to true to see HTTP requests and responses on stdout
        config.setTraceMessage(false);
        BulkConnection connection = new BulkConnection(config);
        return connection;
    }


    /**
     * Create and upload batches using a CSV file.
     * The file into the appropriate size batch files.
     *
     * @param connection
     *            Connection to use for creating batches
     * @param jobInfo
     *            Job associated with new batches
     * @param csvFileName
     *            The source file for batch data
     */
    private List<BatchInfo> createBatchesFromCSVFile(BulkConnection connection,
                                                     JobInfo jobInfo, String csvFileName)
            throws IOException, AsyncApiException {
        List<BatchInfo> batchInfos = new ArrayList<BatchInfo>();
        BufferedReader rdr = new BufferedReader(
                new InputStreamReader(new FileInputStream(csvFileName))
        );
        // read the CSV header row
        byte[] headerBytes = (rdr.readLine() + "\n").getBytes("UTF-8");
        int headerBytesLength = headerBytes.length;
        File tmpFile = File.createTempFile("bulkAPIInsert", ".csv");

        // Split the CSV file into multiple batches
        try {
            FileOutputStream tmpOut = new FileOutputStream(tmpFile);
            int maxBytesPerBatch = 10000000; // 10 million bytes per batch
            int maxRowsPerBatch = 10000; // 10 thousand rows per batch
            int currentBytes = 0;
            int currentLines = 0;
            String nextLine;
            while ((nextLine = rdr.readLine()) != null) {
                byte[] bytes = (nextLine + "\n").getBytes("UTF-8");
                // Create a new batch when our batch size limit is reached
                if (currentBytes + bytes.length > maxBytesPerBatch
                        || currentLines > maxRowsPerBatch) {
                    createBatch(tmpOut, tmpFile, batchInfos, connection, jobInfo);
                    currentBytes = 0;
                    currentLines = 0;
                }
                if (currentBytes == 0) {
                    tmpOut = new FileOutputStream(tmpFile);
                    tmpOut.write(headerBytes);
                    currentBytes = headerBytesLength;
                    currentLines = 1;
                }
                tmpOut.write(bytes);
                currentBytes += bytes.length;
                currentLines++;
            }
            // Finished processing all rows
            // Create a final batch for any remaining data
            if (currentLines > 1) {
                createBatch(tmpOut, tmpFile, batchInfos, connection, jobInfo);
            }
        } finally {
            tmpFile.delete();
        }
        return batchInfos;
    }

    /**
     * Create a batch by uploading the contents of the file.
     * This closes the output stream.
     *
     * @param tmpOut
     *            The output stream used to write the CSV data for a single batch.
     * @param tmpFile
     *            The file associated with the above stream.
     * @param batchInfos
     *            The batch info for the newly created batch is added to this list.
     * @param connection
     *            The BulkConnection used to create the new batch.
     * @param jobInfo
     *            The JobInfo associated with the new batch.
     */
    private void createBatch(FileOutputStream tmpOut, File tmpFile, List<BatchInfo> batchInfos, BulkConnection connection, JobInfo jobInfo)
            throws IOException, AsyncApiException {
        tmpOut.flush();
        tmpOut.close();
        FileInputStream tmpInputStream = new FileInputStream(tmpFile);
        try {
            BatchInfo batchInfo =
                    connection.createBatchFromStream(jobInfo, tmpInputStream);
            System.out.println(batchInfo);
            batchInfos.add(batchInfo);

        } finally {
            tmpInputStream.close();
        }
    }


    public boolean login(BulkConnection bulkConnection) {
        boolean success = false;

        //String userId = userName;
        //String passwd = password;
        //String soapAuthEndPoint = "https://" + loginHost + soapService;
        //String bulkAuthEndPoint = "https://" + loginHost + bulkService;
        try {
            ConnectorConfig config = new ConnectorConfig();
            config.setUsername(Creds.getUsrName());
            config.setPassword(Creds.getPassWrd());
            config.setAuthEndpoint(Creds.getEndpoint());
            config.setCompression(true);
            config.setTraceFile("traceLogs.txt");
            config.setTraceMessage(true);
            config.setPrettyPrintXml(true);
            System.out.println("AuthEndpoint: " + config.getRestEndpoint());
            EnterpriseConnection connection = new EnterpriseConnection(config);
            System.out.println("SessionID: " + config.getSessionId());
            config.setRestEndpoint(Creds.getBulkEndpoint());
            bulkConnection = new BulkConnection(config);
            success = true;
        } catch (AsyncApiException aae) {
            aae.printStackTrace();
        } catch (ConnectionException ce) {
            ce.printStackTrace();
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        }
        return success;
    }

    public void doBulkQuery(BulkConnection bulkConnection) {
        if ( ! login(bulkConnection) ) {
            return;
        }
        try {
            JobInfo job = new JobInfo();
            job.setObject("Attachment");

            job.setOperation(OperationEnum.query);
            job.setConcurrencyMode(ConcurrencyMode.Parallel);
            job.setContentType(ContentType.CSV);

            job = bulkConnection.createJob(job);
            assert job.getId() != null;

            job = bulkConnection.getJobStatus(job.getId());

            String query = "SELECT Id, ParentId, Name, CreatedDate, BodyLength, ContentType FROM Attachment Where BodyLength >= 200000 And BodyLength <= 400000 And ContentType = 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' And ParentId IN (Select Id From Facility_Lease_Agreement__c Where CreatedDate >= 2015-01-01T00:00:00Z AND CreatedDate <= 2016-01-01T00:00:00Z And Status__c = 'Installed/Complete' And RecordTypeId = '01250000000UJtZAAW') limit 1000";

            long start = System.currentTimeMillis();

            BatchInfo info = null;
            ByteArrayInputStream bout = new ByteArrayInputStream(query.getBytes());
            info = bulkConnection.createBatchFromStream(job, bout);

            String[] queryResults = null;

            for(int i=0; i<10000; i++) {
                Thread.sleep(30000); //30 sec
                info = bulkConnection.getBatchInfo(job.getId(), info.getId());

                if (info.getState() == BatchStateEnum.Completed) {
                    System.out.println("QUERY COMPLETED! " + info);
                    QueryResultList list = bulkConnection.getQueryResultList(job.getId(), info.getId());
                    queryResults = list.getResult();
                    break;
                } else if (info.getState() == BatchStateEnum.Failed) {
                    System.out.println("-------------- failed ----------"
                            + info);
                    break;
                } else {
                    System.out.println("-------------- waiting ----------"
                            + info);
                }
            }

            if (queryResults != null) {
                for (String resultId : queryResults) {
                    System.out.println("QUERY RESULT LENGTH: " + queryResults.length);
                    bulkConnection.getQueryResultStream(job.getId(), info.getId(), resultId);
                }
            }
        } catch (AsyncApiException aae) {
            System.out.println("ERROR!");
            aae.printStackTrace();
        } catch (InterruptedException ie) {
            System.out.println("ERROR!");
            ie.printStackTrace();
        }
    }


}