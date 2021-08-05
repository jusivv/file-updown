package org.coodex.file.rest.jaxrs;

import javax.servlet.http.HttpServletRequest;

public interface ISessionGuard {
    boolean pass(HttpServletRequest request);
}
