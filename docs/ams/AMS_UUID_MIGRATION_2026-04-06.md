# AMS UUID-First API Migration (2026-04-06)

## Summary
Attendance APIs are now UUID-first for all external contracts in AMS.
Internal numeric database IDs remain unchanged and are not required by clients.

## Updated Endpoints

### Student attendance records
- `POST /api/v1/auth/ams/records`
- `GET /api/v1/auth/ams/records`
  - filter params: `studentUuid`, `takenByStaffUuid`
  - default sort: `createdAt,desc`
- `GET /api/v1/auth/ams/records/{recordUuid}`
- `PUT /api/v1/auth/ams/records/{recordUuid}`
- `DELETE /api/v1/auth/ams/records/{recordUuid}`

### Staff attendance
- `POST /api/v1/auth/ams/staff`
- `POST /api/v1/auth/ams/staff/bulk`
- `GET /api/v1/auth/ams/staff`
  - filter param: `staffUuid`
  - default sort: `createdAt,desc`
- `GET /api/v1/auth/ams/staff/{recordUuid}`
- `PUT /api/v1/auth/ams/staff/{recordUuid}`
- `DELETE /api/v1/auth/ams/staff/{recordUuid}`

### Attendance types
- `GET /api/v1/auth/ams/types/{typeUuid}`
- `PUT /api/v1/auth/ams/types/{typeUuid}`
- `DELETE /api/v1/auth/ams/types/{typeUuid}`

### Absence documentation / excuses
- `POST /api/v1/auth/ams/excuses/submit`
- `GET /api/v1/auth/ams/excuses/{docUuid}`
- `POST /api/v1/auth/ams/excuses/{docUuid}/approve`
- `POST /api/v1/auth/ams/excuses/{docUuid}/reject`

### Teacher attendance submit flow
- `POST /api/teacher/attendance/mark`
  - now accepts UUID-based student references

## DTO Changes

### Request DTOs
- `StudentAttendanceRequestDTO`
  - new: `studentUuid`, `takenByStaffUuid`
  - deprecated: `studentId`, `takenByStaffId`
- `StaffAttendanceRequestDTO`
  - new: `staffUuid`
  - deprecated: `staffId`
- `SubmitExcuseRequestDTO`
  - new: `attendanceUuid`, `submittedByParentUuid`
  - deprecated: `attendanceId`, `submittedByParentId`

### Response DTOs
- `StudentAttendanceResponseDTO`
  - includes: `uuid`, `studentUuid`, `takenByStaffUuid`
- `StaffAttendanceResponseDTO`
  - includes: `uuid`, `staffUuid`
- `AbsenceDocumentationResponseDTO`
  - includes: `uuid`, `dailyAttendanceUuid`, `submittedByUserUuid`, `approvedByStaffUuid`

## Backward Compatibility
- Numeric request fields are accepted for one release cycle where marked deprecated.
- UUID fields take precedence when both UUID and numeric fields are supplied.

## Deprecation Timeline
- `2026-04-06`: UUID-first enabled, numeric fallback retained.
- `next release +1`: remove deprecated numeric request/response fields from AMS external contracts.

## Example Payloads

### Student attendance create
```json
[
  {
    "studentUuid": "8df36274-9fb9-4302-b224-03c9d9de98fa",
    "attendanceShortCode": "P",
    "attendanceDate": "2026-04-06",
    "takenByStaffUuid": "bcb4f6e3-f40f-40f3-a4cf-cdb7a95fbcfb",
    "notes": "On time"
  }
]
```

### Staff attendance create
```json
{
  "staffUuid": "bcb4f6e3-f40f-40f3-a4cf-cdb7a95fbcfb",
  "attendanceDate": "2026-04-06",
  "attendanceShortCode": "P",
  "source": "MANUAL",
  "notes": "Marked by admin"
}
```

### Submit excuse
```json
{
  "attendanceUuid": "e45073ce-a8e6-42ac-a63b-47b26f522145",
  "submittedByParentUuid": "53ccdcdf-4b3b-4e39-8da4-3ab9655e573f",
  "documentUrl": "https://files.example.com/medical-note.pdf",
  "note": "Medical leave",
  "attendanceDate": "2026-04-06"
}
```

### Student attendance response (sample)
```json
{
  "uuid": "e45073ce-a8e6-42ac-a63b-47b26f522145",
  "studentUuid": "8df36274-9fb9-4302-b224-03c9d9de98fa",
  "studentId": 101,
  "attendanceDate": "2026-04-06",
  "attendanceTypeShortCode": "P",
  "takenByStaffUuid": "bcb4f6e3-f40f-40f3-a4cf-cdb7a95fbcfb",
  "takenByStaffId": 21,
  "notes": "On time"
}
```

### Absence documentation response (sample)
```json
{
  "uuid": "0d3b7689-0bc0-4810-aae0-b847c41840dc",
  "dailyAttendanceUuid": "e45073ce-a8e6-42ac-a63b-47b26f522145",
  "submittedByUserUuid": "53ccdcdf-4b3b-4e39-8da4-3ab9655e573f",
  "approvedByStaffUuid": "bcb4f6e3-f40f-40f3-a4cf-cdb7a95fbcfb",
  "approvalStatus": "APPROVED"
}
```

