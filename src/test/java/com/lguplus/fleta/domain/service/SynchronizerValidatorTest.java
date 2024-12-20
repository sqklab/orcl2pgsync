package com.lguplus.fleta.domain.service;

import com.lguplus.fleta.domain.model.SyncRequestEntity;
import com.lguplus.fleta.domain.service.synchronizer.SynchronizerValidator;
import com.lguplus.fleta.ports.service.SyncRequestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
class SynchronizerValidatorTest {
    @Mock
    SyncRequestService syncRequestService = mock(SyncRequestService.class);

    // Service
    @InjectMocks
	SynchronizerValidator synchronizerValidator;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void noLoopSyncValidation(){
        //given
        SyncRequestEntity firstSyncRequest = new SyncRequestEntity();
        SyncRequestEntity secondSyncRequest = new SyncRequestEntity();

        // First Synchronizer
        firstSyncRequest.setTargetDatabase("dbname");
        firstSyncRequest.setTargetSchema("schemaname");
        firstSyncRequest.setTargetTable("B");
        firstSyncRequest.setSourceDatabase("dbname");
        firstSyncRequest.setSourceSchema("schemaname");
        firstSyncRequest.setSourceTable("A");
        firstSyncRequest.setId(1L);

        List<SyncRequestEntity> emptyList = new ArrayList<>();

        //when ( No Infinite loop case )
        when(syncRequestService.findByTopicNameAndSynchronizerName(anyString(), anyString())).thenReturn(firstSyncRequest);
        when(syncRequestService.findAllRunningSynchronizerWithSourceInformation(
                firstSyncRequest.getTargetDatabase(), firstSyncRequest.getTargetSchema(), firstSyncRequest.getTargetTable())
        ).thenReturn(emptyList);
        boolean noInfiniteLoopresult = synchronizerValidator.syncValidation("test", "test");

        //then ( No Infinite loop case )
        boolean noInfiniteLoopexpected = false;
        assertEquals(noInfiniteLoopexpected, noInfiniteLoopresult);
    }

    @Test
    void pairLoopSyncValidation(){
        //given
        SyncRequestEntity firstSyncRequest = new SyncRequestEntity();
        SyncRequestEntity secondSyncRequest = new SyncRequestEntity();

        // First Synchronizer
        firstSyncRequest.setTargetDatabase("dbname");
        firstSyncRequest.setTargetSchema("schemaname");
        firstSyncRequest.setTargetTable("B");
        firstSyncRequest.setSourceDatabase("dbname");
        firstSyncRequest.setSourceSchema("schemaname");
        firstSyncRequest.setSourceTable("A");
        firstSyncRequest.setId(1L);

        // second Synchronizer
        secondSyncRequest.setTargetDatabase("dbname");
        secondSyncRequest.setTargetSchema("schemaname");
        secondSyncRequest.setTargetTable("A");
        secondSyncRequest.setSourceDatabase("dbname");
        secondSyncRequest.setSourceSchema("schemaname");
        secondSyncRequest.setSourceTable("B");
        secondSyncRequest.setId(4L);

        List<SyncRequestEntity> twoloopList = new ArrayList<>();

        // one-to-one infinite loop
        twoloopList.add(secondSyncRequest);

        // when ( one-to-one Infinite Loop occurs  )
        when(syncRequestService.findByTopicNameAndSynchronizerName(anyString(), anyString())).thenReturn(firstSyncRequest);
        when(syncRequestService.findAllRunningSynchronizerWithSourceInformation(
                firstSyncRequest.getTargetDatabase(), firstSyncRequest.getTargetSchema(), firstSyncRequest.getTargetTable())
        ).thenReturn(twoloopList);
        boolean twoInfiniteLoopresult = synchronizerValidator.syncValidation("test", "test");

        // then ( one-to-one Infinite Loop occurs )
        boolean twoInfiniteLoopexpected = true;
        assertEquals(twoInfiniteLoopexpected, twoInfiniteLoopresult);
    }

    @Test
    void tripleLoopSyncValidation(){
        //given
        SyncRequestEntity firstSyncRequest = new SyncRequestEntity();
        SyncRequestEntity secondSyncRequest = new SyncRequestEntity();
        SyncRequestEntity thirdSyncRequest = new SyncRequestEntity();

        // First Synchronizer
        firstSyncRequest.setSourceDatabase("dbname");
        firstSyncRequest.setSourceSchema("schemaname");
        firstSyncRequest.setSourceTable("A");
        firstSyncRequest.setTargetDatabase("dbname");
        firstSyncRequest.setTargetSchema("schemaname");
        firstSyncRequest.setTargetTable("B");
        firstSyncRequest.setId(1L);

        // Second Synchronizer
        secondSyncRequest.setSourceDatabase("dbname");
        secondSyncRequest.setSourceSchema("schemaname");
        secondSyncRequest.setSourceTable("B");
        secondSyncRequest.setTargetDatabase("dbname");
        secondSyncRequest.setTargetSchema("schemaname");
        secondSyncRequest.setTargetTable("C");
        secondSyncRequest.setId(2L);

        // Third Synchronizer
        thirdSyncRequest.setSourceDatabase("dbname");
        thirdSyncRequest.setSourceSchema("schemaname");
        thirdSyncRequest.setSourceTable("C");
        thirdSyncRequest.setTargetDatabase("dbname");
        thirdSyncRequest.setTargetSchema("schemaname");
        thirdSyncRequest.setTargetTable("A");
        thirdSyncRequest.setId(3L);

        List<SyncRequestEntity> firstloopList = new ArrayList<>();
        List<SyncRequestEntity> secondloopList = new ArrayList<>();

        firstloopList.add(secondSyncRequest);
        secondloopList.add(thirdSyncRequest);

        // when ( three way infinite loop occurs )
        when(syncRequestService.findByTopicNameAndSynchronizerName(anyString(), anyString())).thenReturn(firstSyncRequest);
        when(syncRequestService.findAllRunningSynchronizerWithSourceInformation(
                firstSyncRequest.getTargetDatabase(), firstSyncRequest.getTargetSchema(), firstSyncRequest.getTargetTable())
        ).thenReturn(firstloopList);

        when(syncRequestService.findAllRunningSynchronizerWithSourceInformation(
                firstSyncRequest.getTargetDatabase(), firstSyncRequest.getTargetSchema(), secondSyncRequest.getTargetTable())
        ).thenReturn(secondloopList);

        // then
        boolean threeInfiniteLoopResult = synchronizerValidator.syncValidation("test", "test");
        boolean threeInfiniteLoopExpected = true;
        assertEquals(threeInfiniteLoopExpected, threeInfiniteLoopResult);
    }
}