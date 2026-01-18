# Tổng quan Phần mềm Hỗ trợ Làm Sớ Cúng

## Mô tả chung
Phần mềm Java Swing hỗ trợ xử lý và cập nhật thông tin trong sớ cúng, bao gồm:
- Cập nhật tuổi theo năm
- Quản lý thông tin Sao Hạn theo danh sách đệ tử
- Tạo các file in ấn phục vụ nghi lễ

## Cấu hình
File: `App.config`
- `year`: Năm Dương lịch cần cập nhật (ví dụ: 2026)
- `lunaYear`: Năm Âm lịch (ví dụ: Bính Ngọ)
- `labelSaoHanTemplate`: Mẫu câu cho nhãn in Sao Hạn
- `labelHostTemplate`: Mẫu câu cho nhãn chủ hộ
- `labelHostTemplateForNotebook`: Mẫu câu cho sổ ghi chép

## Cấu trúc giao diện

### Tab 1: "Cập nhật tuổi"
Xử lý file Word sớ với định dạng đặc biệt (văn cổ - đọc từ phải sang trái, mỗi từ một dòng).

**Chức năng:**

1. **Chọn file**
   - Cho phép chọn nhiều file Word (.docx)
   - File sớ có cấu trúc: nhiều trang, nhiều cột, văn bản xuống dòng từng từ tạo cột, đọc từ phải sang trái

2. **Cập nhật tuổi**
   - Tìm pattern tuổi: `[số]t` hoặc `[số] t` (ví dụ: 30t = 30 tuổi)
   - Tự động phát hiện năm của sớ từ pattern `(năm)` ở cuối file
   - Tính chênh lệch: `offset = năm_config - năm_sớ`
   - Cập nhật tất cả tuổi trong sớ: `tuổi_mới = tuổi_cũ + offset`
   - Đồng thời cập nhật năm Phật lịch và năm Âm lịch
   - Tạo file `_updated.docx` và log chi tiết vào `update_log.log`

3. **Format chữ Việt**
   - Tìm tất cả từ tiếng Việt có dấu
   - Format lại theo định dạng thống nhất (Calibri, size 13)
   - Tạo file `_formatted.docx`

4. **Bỏ chọn**
   - Xóa danh sách file đã chọn

**Xử lý:**
- Class: `ContentDocProcessingHelper`
- Input: File Word (.docx)
- Output: File `_updated.docx`, `_formatted.docx`, `update_log.log`

---

### Tab 2: "Hỗ trợ Sao Hạn"
Xử lý danh sách đệ tử từ Excel và tạo các file in ấn.

**Chức năng:**

1. **Chọn file**
   - Chọn 1 file Excel (.xlsx)
   - File chứa danh sách người theo hộ gia đình
   - Cấu trúc Excel (bắt đầu từ dòng 4):
     - Cột C: Số thứ tự hộ
     - Cột D: STT
     - Cột E-G: Họ, tên đệm, tên
     - Cột H: Giới tính (Nam/Nữ)
     - Cột I: Căn mạng (x = true)
     - Cột J: Chủ hộ (x = true)
     - Cột K-L: Thiên Can, Địa Chi
     - Cột M: Năm sinh ước tính
     - Cột N: Tuổi
     - Cột O-P: Sao, Hạn
     - Cột Q: Không hỗ trợ (x = true)
     - Cột R: Địa chỉ
   - Tự động phát hiện dòng bắt đầu (4) và dòng kết thúc

2. **Kiểm tra Sao Hạn**
   - Đọc danh sách từ Excel
   - Tính lại tuổi: `tuổi = năm_config - năm_sinh + 1`
   - Tra cứu Sao chiếu mệnh và Hạn theo tuổi và giới tính (dùng enum)
   - So sánh với giá trị trong Excel, hiển thị lỗi nếu có sai lệch
   - Load dữ liệu vào bộ nhớ để tạo file

3. **Tạo nhãn**
   Tạo 4 loại file từ template:

   a. **printLabelSaoHanGenerated_NAM.docx** và **printLabelSaoHanGenerated_NU.docx**
   - Từ template: `printLabelSaoHanTemplate.docx`
   - Mỗi người ≥ 11 tuổi: 1 trang label
   - Nội dung: Họ tên, tuổi, Sao, Hạn
   - Mục đích: In trên đầu tờ hình nhân
   - Format: `{Họ tên} tuổi {Thiên Can} {Địa Chi} ({tuổi}t) năm nay gặp vận Sao {Sao}, Hạn {Hạn}`

   b. **printCoverGenerated.docx**
   - Từ template: `printCoverTemplate.docx`
   - Mỗi hộ gia đình: 1 hàng trong bảng
   - Nội dung: Số thứ tự hộ, thông tin chủ hộ, số lượng nam/nữ, địa chỉ
   - Mục đích: In mảnh giấy kẹp vào sấp hình nhân từng hộ
   - Format: `{STT}). {Họ tên chủ hộ}, tuổi {Thiên Can} {Địa Chi} {tuổi}t, Gồm có {số người} đệ tử ({số nam} nam, {số nữ} nữ) ở {địa chỉ}`

   c. **printNotebookGenerated.docx**
   - Từ template: `printNotebookTemplate.docx`
   - Bảng 3 cột: STT | Nội Gia | Nội dung cần ghi chép
   - Mỗi hộ: 1 hàng
   - Mục đích: Sổ ghi chép cho từng hộ gia đình

   d. **printSoSaoHanGenerated.docx** (chức năng "Viết Sớ")
   - Từ template: `printSoSaoHanTemplate.docx`
   - Nhóm người theo Sao chiếu mệnh
   - Mỗi nhóm Sao: 1 trang
   - Sắp xếp: Nam trước, Nữ sau
   - Mỗi người: 1 paragraph
   - Format: Họ tên và thông tin tuổi, mỗi từ xuống dòng (văn cổ)

4. **Viết Sớ**
   - Xem mục 3.d bên trên
   - Nhóm theo `SaoChieuEnum`
   - Mỗi trang đảm bảo tối thiểu 58 dòng, tối đa 66 dòng

5. **Bỏ chọn**
   - Reset trạng thái, xóa dữ liệu đã load

**Xử lý:**
- Class: `MotalListProcessingHelper`
- Input: File Excel (.xlsx)
- Output: 4 file Word (.docx)

---

## Cấu trúc code

### Package: `vn.tafi.process`

1. **MainUIProcessor.java**
   - Class chính chứa `main()`
   - Tạo giao diện Swing với `JTabbedPane`
   - Khởi tạo 2 tab: "Cập nhật tuổi" và "Hỗ trợ Sao Hạn"
   - Load config từ `ConfigLoader`
   - Set icon và background cho app

2. **ContentDocProcessingHelper.java**
   - Xử lý file Word sớ
   - `processSelectDocFiles()`: Chọn nhiều file Word
   - `processUpdatingYearAndAge()`: Cập nhật tuổi, năm Phật lịch, năm Âm lịch
   - `processFormatVietChar()`: Format chữ Việt
   - Sử dụng Apache POI XWPF để đọc/ghi Word
   - Regex: `\b(\d+)(?: {1}t|t)\b` để tìm tuổi
   - Regex: `\((\d{4})\)` để tìm năm

3. **MotalListProcessingHelper.java**
   - Xử lý danh sách người từ Excel
   - `processSelectListMotalFile()`: Chọn file Excel
   - `processCheckingSaoHan()`: Kiểm tra Sao Hạn
   - `processCreateLabelAndNote()`: Tạo 3 file (label Nam, Nữ, cover, notebook)
   - `processWritingSo()`: Tạo file sớ Sao Hạn
   - `readMortalObjectsFromExcel()`: Đọc Excel thành `List<MortalObject>`
   - `processMortalObjectsFromExcel()`: Tính lại Sao Hạn và validate
   - Sử dụng Apache POI XSSF để đọc Excel
   - Sử dụng Apache POI XWPF để ghi Word

4. **Utils.java**
   - Utilities chung
   - `getUniqueFileName()`: Tạo tên file không trùng (thêm -01, -02...)
   - `convertNumberToChinese()`: Chuyển số sang chữ Hán
   - `convertNumberToVietnamese()`: Chuyển số sang chữ Nôm
   - `getIntegerValue()`, `getStringValue()`, `getBooleanValue()`: Đọc cell Excel
   - `copyRowStyle()`, `copyCellContent()`: Copy format Word
   - `copyRunFormatting()`: Copy định dạng text
   - `replaceTextWithFormatting()`: Thay text giữ nguyên format
   - `addTableHeader()`: Thêm header cho bảng Word

### Package: `vn.tafi.object`

1. **ConfigLoader.java**
   - Load properties từ `App.config`
   - Singleton pattern
   - `getProperty(key)`: Lấy giá trị config

2. **MortalObject.java**
   - Đại diện cho 1 người trong danh sách
   - Fields: hostOrder, order, fmName, midName, name, gender, thienCan, diaChi, estimatedYearOB, age, sao, han, canMang, isAHost, notSupported, address
   - Calculated fields: ageRecalculated, saoRecalculated, hanRecalculated

3. **SaoChieuEnum.java**
   - Enum các Sao chiếu mệnh
   - `getSaoChieuMang(age, isMale)`: Tra cứu Sao theo tuổi và giới tính

4. **HanEnum.java**
   - Enum các Hạn
   - `getHan(age, isMale)`: Tra cứu Hạn theo tuổi và giới tính

5. **SaoHanGroup.java**
   - Nhóm người theo Sao chiếu mệnh
   - Fields: sao, namMortal, nuMortal, hanSet, countCanMang

6. **MortalDataResultDTO.java**
   - Kết quả validate danh sách
   - Fields: errorObjects, errorMessages

---

## Luồng xử lý chính

### Luồng 1: Cập nhật tuổi trong sớ Word

```
1. User chọn file Word sớ
2. User click "Cập nhật tuổi"
3. Popup hỏi năm của sớ (hoặc để trống tự phát hiện)
4. Đọc config: year, lunaYear
5. Tính Phật lịch = year + 544
6. Nếu không nhập năm:
   - Tìm pattern (năm) trong sớ
   - offset = Phật_lịch - năm_tìm_thấy
7. Nếu có nhập năm:
   - offset = Phật_lịch - (năm_nhập + 544)
8. Duyệt tất cả paragraph:
   - Tìm pattern [số]t
   - Cộng thêm offset
   - Giữ nguyên format
9. Cập nhật năm Phật lịch (14 dòng số chữ Hán + chữ Nôm)
10. Cập nhật năm Âm lịch (2 chữ)
11. Lưu file _updated.docx
12. Log chi tiết vào update_log.txt
```

### Luồng 2: Tạo nhãn Sao Hạn từ Excel

```
1. User chọn file Excel
2. Auto phát hiện dòng bắt đầu (4) và kết thúc
3. User click "Kiểm tra Sao Hạn"
4. Đọc Excel từ dòng start → end
5. Với mỗi dòng:
   - Parse thành MortalObject
   - Tính tuổi = year_config - estimatedYearOB + 1
   - Tra Sao = SaoChieuEnum.getSaoChieuMang(tuổi, isMale)
   - Tra Hạn = HanEnum.getHan(tuổi, isMale)
   - So sánh với Excel, ghi lỗi nếu sai
6. Hiển thị kết quả validate
7. User click "Tạo nhãn"
8. Filter: chỉ lấy người ≥ 11 tuổi và notSupported = false
9. Phân loại:
   - maleList, femaleList (cho label Sao Hạn)
   - groupedByHostOrder (cho cover và notebook)
10. Tạo 4 file:
    - printLabelSaoHanGenerated_NAM.docx
    - printLabelSaoHanGenerated_NU.docx
    - printCoverGenerated.docx
    - printNotebookGenerated.docx
11. User click "Viết Sớ"
12. Nhóm theo Sao chiếu mệnh
13. Tạo file printSoSaoHanGenerated.docx
```

---

## Công nghệ sử dụng

- **Java Swing**: Giao diện desktop
- **Apache POI 5.x**: Đọc/ghi Excel (XSSF) và Word (XWPF)
- **Java NIO**: Đọc/ghi file
- **Regex**: Pattern matching cho tuổi, năm

---

## File templates cần có

1. `printLabelSaoHanTemplate.docx`: Template cho label Sao Hạn
2. `printCoverTemplate.docx`: Template cho cover hộ gia đình (có bảng)
3. `printNotebookTemplate.docx`: Template cho sổ ghi chép (có bảng)
4. `printSoSaoHanTemplate.docx`: Template cho sớ Sao Hạn (paragraph định dạng đặc biệt)

---

## Đặc điểm kỹ thuật

### Xử lý Word
- Giữ nguyên định dạng (font, size, bold, italic, color, spacing)
- Copy format từ template khi tạo nội dung mới
- Xử lý bảng: copy row style, cell style
- Page break giữa các nhóm
- Đảm bảo số dòng tối thiểu/tối đa mỗi trang (58-66)

### Xử lý Excel
- Đọc formula với FormulaEvaluator
- Xử lý cell: String, Numeric, Boolean, Formula
- Auto-detect vùng dữ liệu (tìm dòng trống)

### Format số
- Chuyển đổi số sang chữ Hán và chữ Nôm cho năm Phật lịch
- Ví dụ: 2570 → "二 千 五 百 七 十" (Hán) và "Nhị Thiên Ngũ Bách Thất Thập" (Nôm)

### Validation
- Kiểm tra tuổi, Sao, Hạn có khớp không
- Ghi log chi tiết các thay đổi
- Hiển thị lỗi rõ ràng cho user

---

## Lưu ý quan trọng

1. **Định dạng sớ**: Văn cổ, đọc từ phải sang trái, mỗi từ một dòng
2. **Điều kiện lọc**: Chỉ xử lý người ≥ 11 tuổi cho label Sao Hạn
3. **notSupported flag**: Người có flag này không được tính Sao Hạn nhưng vẫn xuất hiện trong notebook
4. **Unique filename**: Tự động đánh số -01, -02 nếu file đã tồn tại
5. **Error handling**: Validate đầy đủ trước khi tạo file, hiển thị lỗi chi tiết
6. **Encoding**: UTF-8 cho tiếng Việt, Unicode cho chữ Hán
