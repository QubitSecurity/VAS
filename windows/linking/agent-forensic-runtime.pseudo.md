# Agent Forensic Runtime Pseudocode

## Goal

The agent performs all extraction. It reads the detection event, checks whether the matched
filter has `forensic.isUse=true`, loads the separate forensic meta for that detection, selects
an analyzer route by `osVariant`, applies grok/regex extraction rules, and sends a forensic
payload. Backend and ETL only store the result.

---

## Inputs

- `event`: original detection event
- `matchedFilter`: detection filter JSON
- `forensicMeta`: separate per-detection forensic meta JSON
- `extractionRules`: analyst-managed YAML rules
- `hostContext`: local agent context (`osType`, `osVariant`, hostname, agent version)

---

## Pseudocode

```text
function handleDetectionForensic(event, matchedFilter, forensicMeta, extractionRules, hostContext):

    if matchedFilter is null:
        return NOOP

    if matchedFilter.forensic is null:
        return NOOP

    if matchedFilter.forensic.isUse != true:
        return NOOP

    if forensicMeta is null:
        log_warn("forensic meta not found", matchedFilter.id)
        return NOOP

    if forensicMeta.forensic is null:
        return NOOP

    if forensicMeta.forensic.isUse != true:
        return NOOP

    selectedVariant = selectVariant(
        forensicMeta.forensic.forensicOsVariants,
        hostContext.osVariant
    )

    if selectedVariant is null:
        log_debug("no forensic osVariant route", hostContext.osVariant)
        return NOOP

    for each forensicValue in selectedVariant.forensicValues:

        forensicType = forensicValue.forensicType
        forensicId = forensicValue.forensicId
        needForensicValue = forensicValue.needForensicValue

        extracted = {}

        if needForensicValue == true:
            extractor = findBestExtractor(
                extractionRules,
                matchedFilter.id,
                forensicId,
                hostContext.osType,
                hostContext.osVariant,
                event.eventKey
            )

            if extractor is null:
                log_warn("extractor not found", matchedFilter.id, forensicId)
                continue

            parseResult = runExtractor(extractor, event)

            if parseResult.matched != true:
                log_warn("extractor did not match", extractor.id)
                continue

            extracted = applyPostProcess(extractor.post_process, parseResult.fields)

            if extracted is empty:
                log_warn("extraction returned empty result", extractor.id)
                continue

        payload = {
            "eventId": event.id,
            "detectionId": matchedFilter.id,
            "forensicType": forensicType,
            "forensicId": forensicId,
            "osType": hostContext.osType,
            "osVariant": hostContext.osVariant,
            "hostname": hostContext.hostname,
            "sourceEventKey": event.eventKey,
            "sourceProvider": event.eventProviderKey,
            "needForensicValue": needForensicValue,
            "forensicValues": extracted,
            "collectedAt": nowUtcIso8601()
        }

        sendForensicPayload(payload)

    return OK
```

---

## Helper Logic

```text
function selectVariant(variants, osVariant):
    for each item in variants:
        if lowercase(item.osVariant) == lowercase(osVariant):
            return item
    return null
```

```text
function findBestExtractor(rules, detectionId, forensicId, osType, osVariant, eventKey):
    candidates = []

    for each rule in rules.extractors:
        if rule.enabled != true:
            continue

        if rule.match.detection_ids exists and detectionId not in rule.match.detection_ids:
            continue

        if rule.match.forensic_ids exists and forensicId not in rule.match.forensic_ids:
            continue

        if rule.match.os_types exists and osType not in rule.match.os_types:
            continue

        if rule.match.forensic_os_variants exists and osVariant not in rule.match.forensic_os_variants:
            continue

        if rule.match.event_keys exists and eventKey not in rule.match.event_keys:
            continue

        candidates.append(rule)

    if candidates is empty:
        return null

    return candidates[0]
```

```text
function runExtractor(extractor, event):
    parsed = {}

    for each parser in extractor.parsers:
        value = getFieldValue(event, parser.field)
        if value is null or value == "":
            continue

        if parser.type == "regex":
            m = regex_match(parser.pattern, value)
            if m matched:
                parsed = mapOutputs(parser.outputs, m.named_groups)
                return { matched: true, fields: parsed, parser_id: parser.id }

        if parser.type == "grok":
            g = grok_match(parser.pattern, value)
            if g matched:
                parsed = mapOutputs(parser.outputs, g.fields)
                return { matched: true, fields: parsed, parser_id: parser.id }

    return { matched: false, fields: {} }
```

```text
function applyPostProcess(steps, fields):
    result = clone(fields)

    for each step in steps:
        if step startsWith "trim:":
            field = suffix(step, "trim:")
            result[field] = trim(result[field])

        else if step startsWith "lowercase:":
            field = suffix(step, "lowercase:")
            result[field] = lowercase(result[field])

        else if step startsWith "strip_wrapping_quotes:":
            field = suffix(step, "strip_wrapping_quotes:")
            result[field] = stripWrappingQuotes(result[field])

        else if step startsWith "normalize_windows_path:":
            field = suffix(step, "normalize_windows_path:")
            result[field] = normalizeWindowsPath(result[field])

        else if step startsWith "normalize_linux_path:":
            field = suffix(step, "normalize_linux_path:")
            result[field] = normalizeLinuxPath(result[field])

        else if step startsWith "set:":
            pair = suffix(step, "set:")
            key, value = splitFirst(pair, "=")
            result[key] = value

    return removeEmptyValues(result)
```

---

## Notes

- `forensic.isUse` stays in the detection filter for fast enable/disable.
- The detailed route (`forensicId`, `needForensicValue`, `osVariant`) stays in separate forensic meta.
- The analyst-managed YAML defines only extraction logic.
- The agent is the only component that executes regex/grok extraction.
- Backend and ETL store the delivered forensic payload as-is.
