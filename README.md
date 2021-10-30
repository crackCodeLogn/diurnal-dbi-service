# diurnal-dbi-service

### Tech stack:

<ol>
<li>Java 8 (as Heroku doesn't support higher buildpacks by default)</li>
<li>Spring-Boot</li>
<li>Heroku</li>
<li>Postgres - for prod</li>
<li>CockroachDB - for local dev. CRDB is built on postgres. <a href="https://www.cockroachlabs.com">https://www.cockroachlabs.com/</a></li>
<li>Spring fox - Swagger</li>
<li>OpenFeign</li>
<li>Gson</li>
<li>Protobuf</li>
<li>Lombok</li>
<li>Apache Commons Lang3</li>
<li>Assertj</li>
</ol> 

### Important notepoints:

<ul>
<li>Database is using timestamptz to store all time in UTC by default. Whenever the java process will read it from DB, it will get the time converted into the JVM time.</li>
</ul> 

## Deployments - Server only (!app):

| Serial | Version | Time | Artifact | Significant changes | 
| ------ | ------- | ---- | -------- | ------------------- |
| 3      | 1.3     | 2021-10-30 1000 IST   | 0.0.4-SNAPSHOT | Backup and upload to github, restricting cloud backup rows to 1 year from last date and overhauling configs |
| 2      | 1.2     | 2021-10-28 2330 IST   | 0.0.3-SNAPSHOT | Using JPA completely and replacing the hand-written SQL logic |
| 1      | 1.1     | 2021-10-24   | 0.0.2-SNAPSHOT | Giving 1 month free premium status to new sign ups |
| 0      | 1.0     | 2021-06-08   | 0.0.1-SNAPSHOT | Genesis and deployed to Heroku-free tier |

## Backup option from postgres:-

psql <DATABASE_URL> \copy (select * from <table_name) to '<local_file.csv>' with CSV;

## Backup restore into postgres:-

I had to write customized code to read csv and use the upload endpoint to bulk add.
