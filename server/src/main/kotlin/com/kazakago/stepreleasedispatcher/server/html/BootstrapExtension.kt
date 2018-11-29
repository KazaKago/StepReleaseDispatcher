package com.kazakago.stepreleasedispatcher.server.html

import kotlinx.html.*

fun HEAD.addBootstrapMetadata() {
    meta(charset = "utf-8")
    meta(name = "viewport", content = "width=device-width, initial-scale=1, shrink-to-fit=no")
    styleLink("/webjars/bootstrap/css/bootstrap.min.css")
}

fun BODY.addBootstrapScript() {
    script(src = "/webjars/jquery/jquery.min.js") {}
    script(src = "/webjars/popper.js/umd/popper.min.js") {}
    script(src = "/webjars/bootstrap/js/bootstrap.min.js") {}
}