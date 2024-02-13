## 1. Solr

### 1.1 Run

    ~/solr/bin/solr start -cloud

    ~/solr/bin/solr stop

## 2. Solr

### 2.1 Add-field-type

nodev와 nosuid 옵션이 적절하게 설정되지 않은 파일 시스템을 찾았을 때 "F"를 출력
```
#!/bin/bash

# 점검할 파일 시스템 목록
check_filesystems=(
  "/dev/shm"
  "/tmp"
  "/var/tmp"
  "/home"
)

# 전체 점검 결과를 저장할 변수
overall_result="T"

# nodev와 nosuid 옵션 점검
for fs in "${check_filesystems[@]}"; do
  # 마운트된 파일 시스템의 옵션 확인
  mount_options=$(mount | grep " on $fs " | awk '{print $6}')
  
  # nodev 옵션 점검
  if [[ $mount_options != *nodev* ]]; then
    echo "F - $fs does not have 'nodev' option set."
    overall_result="F"
    # 문제가 있는 파일 시스템의 상세 정보 출력
    ls -ld $fs
  fi
  
  # nosuid 옵션 점검
  if [[ $mount_options != *nosuid* ]]; then
    echo "F - $fs does not have 'nosuid' option set."
    overall_result="F"
    # 문제가 있는 파일 시스템의 상세 정보 출력
    ls -ld $fs
  fi
done

# 모든 파일 시스템이 적절하게 설정되었는지 최종 결과 출력
if [ $overall_result == "T" ]; then
  echo "T - All filesystems are correctly configured with 'nodev' and 'nosuid' options."
else
  echo "F - One or more filesystems are incorrectly configured."
fi
```
<hr/>
### 2.2 Replace-field

```
curl -X POST -H 'Content-type:application/json' --data-binary '{
  "replace-field":{
     "name":"logMessage",
     "type":"keyword_analysis",
     "multiValued":false,
     "indexed":true,
     "required":false,
     "stored":true}
}' http://localhost:8983/solr/syslog/schema
```

<hr/>

```
{
  "replace-field-type": {
    "name": "msg_analysis",
    "class": "solr.TextField",
    "positionIncrementGap": "100",
    "indexAnalyzer": {
      "charFilters": [
          {
            "class": "solr.PatternReplaceCharFilterFactory",
            "pattern": "\"",
            "replacement": ""
          },
          {
            "class": "solr.PatternReplaceCharFilterFactory",
            "pattern": "'",
            "replacement": ""
          },
          {
            "class": "solr.PatternReplaceCharFilterFactory",
            "pattern": "msg=audit\\([^)]+\\):",
            "replacement": ""
          },
          {
            "class": "solr.PatternReplaceCharFilterFactory",
            "pattern": "msg=",
            "replacement": ""
          }
      ],
      "tokenizer": {
        "class": "solr.StandardTokenizerFactory"
      },
      "filters": [
        {
          "class": "solr.LowerCaseFilterFactory"
        },
        {
          "class": "solr.EdgeNGramFilterFactory",
          "maxGramSize": "20",
          "minGramSize": "2"
        },
        {
          "class": "solr.StopFilterFactory",
          "ignoreCase": "true",
          "words": "stopwords.txt"
        },
        {
          "class": "solr.SynonymGraphFilterFactory",
          "synonyms": "synonyms.txt",
          "ignoreCase": "true",
          "expand": "true"
        }
      ]
    },
    "queryAnalyzer": {
      "tokenizer": {
        "class": "solr.KeywordTokenizerFactory"
      },
      "filters": [
        {
          "class": "solr.LowerCaseFilterFactory"
        },
        {
          "class": "solr.SynonymGraphFilterFactory",
          "synonyms": "synonyms.txt",
          "ignoreCase": "true",
          "expand": "true"
        }
      ]
    }
  }
}
```

<hr/>

```
curl -X POST -H "Content-type: application/json" --data-binary @schema_syslog_msg.json http://localhost:8983/solr/syslog/schema
```

<hr/>

```
c
```

<hr/>

```
d
```
