package com.pulsedesk.service;

import com.pulsedesk.model.Comment;
import com.pulsedesk.model.TriageResult;

public interface TriageService {
    TriageResult analyze(Comment comment);
}
