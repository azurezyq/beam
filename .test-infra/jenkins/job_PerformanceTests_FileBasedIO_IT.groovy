/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import CommonJobProperties as commonJobProperties

def testsConfigurations = [
        [
                jobName           : 'beam_PerformanceTests_TextIOIT',
                jobDescription    : 'Runs PerfKit tests for TextIOIT',
                itClass           : 'org.apache.beam.sdk.io.text.TextIOIT',
                bqTable           : 'beam_performance.textioit_pkb_results',
                prCommitStatusName: 'Java TextIO Performance Test',
                prTriggerPhase    : 'Run Java TextIO Performance Test',
                extraPipelineArgs: [
                        bigQueryDataset: 'beam_performance',
                        bigQueryTable: 'textioit_results',
                        numberOfRecords: '1000000'
                ]

        ],
        [
                jobName            : 'beam_PerformanceTests_Compressed_TextIOIT',
                jobDescription     : 'Runs PerfKit tests for TextIOIT with GZIP compression',
                itClass            : 'org.apache.beam.sdk.io.text.TextIOIT',
                bqTable            : 'beam_performance.compressed_textioit_pkb_results',
                prCommitStatusName : 'Java CompressedTextIO Performance Test',
                prTriggerPhase     : 'Run Java CompressedTextIO Performance Test',
                extraPipelineArgs: [
                        bigQueryDataset: 'beam_performance',
                        bigQueryTable: 'compressed_textioit_results',
                        numberOfRecords: '1000000',
                        compressionType: 'GZIP'
                ]
        ],
        [
                jobName           : 'beam_PerformanceTests_ManyFiles_TextIOIT',
                jobDescription    : 'Runs PerfKit tests for TextIOIT with many output files',
                itClass           : 'org.apache.beam.sdk.io.text.TextIOIT',
                bqTable           : 'beam_performance.many_files_textioit_pkb_results',
                prCommitStatusName: 'Java ManyFilesTextIO Performance Test',
                prTriggerPhase    : 'Run Java ManyFilesTextIO Performance Test',
                extraPipelineArgs: [
                        bigQueryDataset: 'beam_performance',
                        bigQueryTable: 'many_files_textioit_results',
                        reportGcsPerformanceMetrics: 'true',
                        gcsPerformanceMetrics: 'true',
                        numberOfRecords: '1000000',
                        numberOfShards: '1000'
                ]

        ],
        [
                jobName           : 'beam_PerformanceTests_AvroIOIT',
                jobDescription    : 'Runs PerfKit tests for AvroIOIT',
                itClass           : 'org.apache.beam.sdk.io.avro.AvroIOIT',
                bqTable           : 'beam_performance.avroioit_pkb_results',
                prCommitStatusName: 'Java AvroIO Performance Test',
                prTriggerPhase    : 'Run Java AvroIO Performance Test',
                extraPipelineArgs: [
                        numberOfRecords: '1000000',
                        bigQueryDataset: 'beam_performance',
                        bigQueryTable: 'avroioit_results',
                ]
        ],
        [
                jobName           : 'beam_PerformanceTests_TFRecordIOIT',
                jobDescription    : 'Runs PerfKit tests for beam_PerformanceTests_TFRecordIOIT',
                itClass           : 'org.apache.beam.sdk.io.tfrecord.TFRecordIOIT',
                bqTable           : 'beam_performance.tfrecordioit_pkb_results',
                prCommitStatusName: 'Java TFRecordIO Performance Test',
                prTriggerPhase    : 'Run Java TFRecordIO Performance Test',
                extraPipelineArgs: [
                        bigQueryDataset: 'beam_performance',
                        bigQueryTable: 'tfrecordioit_results',
                        numberOfRecords: '1000000'
                ]
        ],
        [
                jobName           : 'beam_PerformanceTests_XmlIOIT',
                jobDescription    : 'Runs PerfKit tests for beam_PerformanceTests_XmlIOIT',
                itClass           : 'org.apache.beam.sdk.io.xml.XmlIOIT',
                bqTable           : 'beam_performance.xmlioit_pkb_results',
                prCommitStatusName: 'Java XmlIOPerformance Test',
                prTriggerPhase    : 'Run Java XmlIO Performance Test',
                extraPipelineArgs: [
                        bigQueryDataset: 'beam_performance',
                        bigQueryTable: 'xmlioit_results',
                        numberOfRecords: '100000000',
                        charset: 'UTF-8'
                ]
        ],
        [
                jobName           : 'beam_PerformanceTests_ParquetIOIT',
                jobDescription    : 'Runs PerfKit tests for beam_PerformanceTests_ParquetIOIT',
                itClass           : 'org.apache.beam.sdk.io.parquet.ParquetIOIT',
                bqTable           : 'beam_performance.parquetioit_pkb_results',
                prCommitStatusName: 'Java ParquetIOPerformance Test',
                prTriggerPhase    : 'Run Java ParquetIO Performance Test',
                extraPipelineArgs: [
                        bigQueryDataset: 'beam_performance',
                        bigQueryTable: 'parquetioit_results',
                        numberOfRecords: '100000000'
                ]
        ]
]

for (testConfiguration in testsConfigurations) {
    create_filebasedio_performance_test_job(testConfiguration)
}


private void create_filebasedio_performance_test_job(testConfiguration) {

    // This job runs the file-based IOs performance tests on PerfKit Benchmarker.
    job(testConfiguration.jobName) {
        description(testConfiguration.jobDescription)

        // Set default Beam job properties.
        commonJobProperties.setTopLevelMainJobProperties(delegate)

        // Allows triggering this build against pull requests.
        commonJobProperties.enablePhraseTriggeringFromPullRequest(
                delegate,
                testConfiguration.prCommitStatusName,
                testConfiguration.prTriggerPhase)

        // Run job in postcommit every 6 hours, don't trigger every push, and
        // don't email individual committers.
        commonJobProperties.setAutoJob(
                delegate,
                'H */6 * * *')

        def pipelineOptions = [
                project        : 'apache-beam-testing',
                tempRoot       : 'gs://temp-storage-for-perf-tests',
                filenamePrefix : "gs://temp-storage-for-perf-tests/${testConfiguration.jobName}/\${BUILD_ID}/",
        ]
        if (testConfiguration.containsKey('extraPipelineArgs')) {
            pipelineOptions << testConfiguration.extraPipelineArgs
        }

        def argMap = [
                benchmarks           : 'beam_integration_benchmark',
                beam_it_timeout      : '1200',
                beam_prebuilt        : 'false',
                beam_sdk             : 'java',
                beam_it_module       : ':sdks:java:io:file-based-io-tests',
                beam_it_class        : testConfiguration.itClass,
                beam_it_options      : commonJobProperties.joinPipelineOptions(pipelineOptions),
                beam_extra_properties: '["filesystem=gcs"]',
                bigquery_table       : testConfiguration.bqTable,
        ]
        commonJobProperties.buildPerformanceTest(delegate, argMap)
    }
}
