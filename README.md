# diurnal-dbi-service

### Tech stack:

<ol>
<li>Java 8 (as Heroku doesn't support higher buildpacks by default)</li>
<li>Spring-Boot</li>
<li>Heroku</li>
<li>Postgres - for prod</li>
<li>CockroachDB - for local dev. CRDB is built on postgres. <a href="https://www.cockroachlabs.com">https://www.cockroachlabs.com/</a></li>
<li>Spring fox - Swagger</li>
<li>Lombok</li>
<li>Gson</li>
<li>Protobuf</li>
<li>Apache Commons Lang3</li>
<li>Assertj</li>
</ol> 

### Important notepoints:

<ul>
<li>Database is using timestamptz to store all time in UTC by default. Whenever the java process will read it from DB, it will get the time converted into the JVM time.</li>
</ul> 

## Deployments - Server only (!app):

### 1. Release 1.2:

    Time: 2021-10-28 2330 IST
    Artifact version: 0.0.3-SNAPSHOT
    Most significant change: Using JPA completely and replacing the hand-written SQL logic

### 2. Release 1.1:

    Time: 2021-10-24
    Artifact version: 0.0.2-SNAPSHOT
    Most significant change: Giving 1 month free premium status to new sign ups

## Backup option from postgres:-

psql <DATABASE_URL> \copy (select * from <table_name) to '<local_file.csv>' with CSV;

## Backup restore into postgres:-

I had to write customized code to red csv and use the upload endpoint to bulk add.