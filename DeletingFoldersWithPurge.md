# Introduction #
Facing recurring hard drive space problems on our build server, we pondered several solutions to delete outdated builds. Purge came out as a winner.


# Details #
Objective: Each night, delete all old builds, but keep the three most recent.

First thought: "Cron". But then again, simple "delete all older" cron jobs don't know whether the build was successful, what stuff to keep.

An Ant-based task would be more suited to the task, since failing builds would keep it from execution. Browsing the web, we came across purge: Using this task, getting rid of those old builds is as simple as setting "deletefolders" to "true". That way, the oldest folders from the given filesets are removed and the more recent remain. Files (except files within those folders) remain untouched.