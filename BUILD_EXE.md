# Hướng dẫn Build Sovan Windows EXE Installer

## Tổng quan

Hướng dẫn này giúp bạn tạo Windows EXE installer cho Sovan chạy độc lập mà không cần cài đặt Java.

## Yêu cầu

- **Java 18+** (đã cài đặt)
- **Maven 3.6+** (đã cài đặt)
- **Windows 10+**
- **WiX Toolset 3.0+** (tùy chọn, để tạo EXE installer)

## Chuẩn bị

Đảm bảo các file sau nằm ở thư mục gốc project:
- `App.config` - File cấu hình ứng dụng
- `printCoverTemplate.docx` - Mẫu in nhãn chủ hộ
- `printLabelSaoHanTemplate.docx` - Mẫu in nhãn Sao Hạn
- `printNotebookTemplate.docx` - Mẫu in ghi chú
- `printSoSaoHanTemplate.docx` - Mẫu in Sớ Sao Hạn
- `build-exe.bat` - Script build

## Build Process

### Bước 1: Chạy Build Script

```bash
.\build-exe.bat
```

Script sẽ tự động thực hiện:
- **[0/4]** Copy `App.config` và 4 template files vào `src/main/resources`
- **[1/4]** Build JAR fat với Maven (bao gồm tất cả dependencies)
- **[2/4]** Copy `App.config` và template files vào `target` folder
- **[3/4]** Chạy jpackage để tạo EXE installer hoặc portable app
- **[4/4]** Verify output

### Bước 2: Output

Sau khi build thành công, bạn sẽ có output ở `target/dist/`:

**Portable App (không cần WiX):**
```
target\dist\Sovan\
├── Sovan.exe                    (file chạy)
├── Sovan.bat
├── App.config                   (file cấu hình)
├── printCoverTemplate.docx      (template)
├── printLabelSaoHanTemplate.docx
├── printNotebookTemplate.docx
├── printSoSaoHanTemplate.docx
├── lib\                         (dependencies)
└── runtime\                     (bundled Java)
```

**EXE Installer (cần WiX):**
```
target\dist\Sovan-0.0.2.exe
```

### Bước 3: Kiểm tra và Cài đặt

Chạy EXE installer hoặc portable app để test:
```
target\dist\Sovan\Sovan.exe
```

## File Configuration

### App.config

File `App.config` là file cấu hình ứng dụng. Nó được tìm kiếm theo thứ tự ưu tiên:

1. **Thư mục cài đặt ứng dụng** (ưu tiên cao nhất)
   ```
   C:\Program Files\TAFI\Sovan\App.config
   ```

2. **Thư mục chứa JAR**
   ```
   Nơi file Sovan.exe được cài đặt
   ```

3. **Resources trong JAR** (fallback)

4. **Working directory**

**Sau khi cài đặt, bạn có thể chỉnh sửa `App.config`:**
- Mở file bằng text editor
- Chỉnh sửa các giá trị
- Lưu file (Ctrl+S)
- Khởi động lại ứng dụng

| Key | Ý nghĩa |
|-----|---------|
| `year` | Năm được cấu hình |
| `lunaYear` | Tên năm âm lịch |
| `labelSaoHanTemplate` | Mẫu câu cho nhãn in |
| `labelHostTemplate` | Mẫu câu cho chủ hộ |
| `labelHostTemplateForNotebook` | Mẫu câu cho "Sổ Ghi Chép" |
| `summaryTitleForGroupSao` | Tiêu đề cho bản tóm tắt |

## Template Files & Output

### Cách tìm Template

Khi ứng dụng cần template, nó tìm theo thứ tự ưu tiên:

1. **Cùng thư mục với file Excel được chọn** ⭐ (Ưu tiên cao nhất)
   - User có thể customize template cho từng dự án
   - Không cần cài đặt lại ứng dụng

2. **Thư mục cài đặt ứng dụng**
   ```
   C:\Program Files\TAFI\Sovan\printCoverTemplate.docx
   ```
   - Template chuẩn được include trong EXE

3. **Resources trong JAR** (fallback)

4. **Working directory**

### Output Files

Tất cả file output được lưu **cùng thư mục với file Excel được chọn:**

```
C:\Users\John\Documents\
├── data.xlsx                           ← File Excel được chọn
├── printCoverTemplate.docx             ← Template (optional)
├── printLabelSaoHanGenerated_NAM.docx  ← Output
├── printLabelSaoHanGenerated_NU.docx   ← Output
├── printCoverGenerated.docx            ← Output
├── printNotebookGenerated.docx         ← Output
└── printSoSaoHanGenerated.docx         ← Output
```

**Lợi ích:**
- Tất cả file tổ chức ngăn nắp
- Dễ backup, share, version control
- Không cần tìm kiếm ở nhiều nơi

### Workflow Example

**Scenario 1: User có template cùng thư mục Excel**
1. User chọn file: `D:\Projects\data.xlsx`
2. Thư mục `D:\Projects\` có template files
3. Ứng dụng sử dụng template từ `D:\Projects\`
4. Output lưu vào `D:\Projects\`

**Scenario 2: User không có template**
1. User chọn file: `C:\Users\John\data.xlsx`
2. Thư mục `C:\Users\John\` không có template
3. Ứng dụng tìm template ở `C:\Program Files\TAFI\Sovan\`
4. Output lưu vào `C:\Users\John\`

## Tạo EXE Installer (Tùy chọn)

Nếu muốn tạo EXE installer thay vì portable app:

### Cài đặt WiX Toolset

1. Tải WiX 3.0 từ: https://wixtoolset.org
2. Cài đặt WiX Toolset
3. Restart máy tính
4. Chạy lại `build-exe.bat`
`powershell -Command "& '[path]/build-exe.bat'" 2>&1`

Script sẽ tự động detect WiX và tạo EXE installer.

## Troubleshooting

### Lỗi: "jpackage not found"
- Đảm bảo Java 18+ được cài đặt
- Chạy: `java -version` để kiểm tra
- jpackage phải nằm trong Java bin folder

### Lỗi: Maven build failed
- Đảm bảo có internet để download dependencies
- Xóa thư mục `.m2\repository` và thử lại
- Chạy: `mvn clean compile` để kiểm tra

### App.config không nằm cạnh file chạy
- Kiểm tra `App.config` ở thư mục gốc project
- Kiểm tra `build-exe.bat` có copy file không
- Xem console log để biết đường dẫn load từ đâu

### Template không được tìm thấy
- Kiểm tra console log để thấy vị trí tìm kiếm
- Đảm bảo tên file đúng: `printCoverTemplate.docx`
- Thử copy template vào cùng thư mục Excel

### Output không xuất hiện ở thư mục mong đợi
- Kiểm tra đã chọn file Excel chưa
- Mở log để xem thư mục output
- Kiểm tra quyền ghi file

## Cấu trúc Folder Sau Cài Đặt

### EXE Installer
```
C:\Program Files\TAFI\Sovan\
├── Sovan.exe                    ← File chạy
├── App.config                   ← Cấu hình (có thể chỉnh sửa)
├── printCoverTemplate.docx      ← Template
├── printLabelSaoHanTemplate.docx
├── printNotebookTemplate.docx
├── printSoSaoHanTemplate.docx
├── lib\                         ← Dependencies
└── runtime\                     ← Bundled Java
```

### Portable App
```
target\dist\Sovan\
├── Sovan.exe
├── Sovan.bat
├── App.config
├── printCoverTemplate.docx
├── printLabelSaoHanTemplate.docx
├── printNotebookTemplate.docx
├── printSoSaoHanTemplate.docx
├── lib\
└── runtime\
```

## Các File Liên Quan

| File | Mục đích |
|------|---------|
| `build-exe.bat` | Script build chính |
| `App.config` | File cấu hình ứng dụng |
| `pom.xml` | Maven configuration |
| `printCoverTemplate.docx` | Template in nhãn |
| `printLabelSaoHanTemplate.docx` | Template in nhãn Sao Hạn |
| `printNotebookTemplate.docx` | Template in ghi chú |
| `printSoSaoHanTemplate.docx` | Template in Sớ |

## Manual Build (Nâng cao)

Nếu muốn kiểm soát từng bước:

```bash
# Step 1: Copy files vào resources
copy App.config src\main\resources\App.config
copy printCoverTemplate.docx src\main\resources\printCoverTemplate.docx
copy printLabelSaoHanTemplate.docx src\main\resources\printLabelSaoHanTemplate.docx
copy printNotebookTemplate.docx src\main\resources\printNotebookTemplate.docx
copy printSoSaoHanTemplate.docx src\main\resources\printSoSaoHanTemplate.docx

# Step 2: Build JAR
mvn clean package

# Step 3: Copy files vào target
copy App.config target\App.config
copy printCoverTemplate.docx target\printCoverTemplate.docx
copy printLabelSaoHanTemplate.docx target\printLabelSaoHanTemplate.docx
copy printNotebookTemplate.docx target\printNotebookTemplate.docx
copy printSoSaoHanTemplate.docx target\printSoSaoHanTemplate.docx

# Step 4: Run jpackage
cd target
jpackage --input . --name Sovan --main-jar Sovan-0.0.2-SNAPSHOT.jar --main-class vn.tafi.process.MainUIProcessor --type app-image --vendor TAFI --app-version 0.0.2 --resource-dir . --dest dist
cd ..
```

## Notes

- EXE file size khoảng 100-200 MB (bao gồm bundled Java)
- Ứng dụng chạy mà không cần Java cài đặt
- App.config và template files có thể chỉnh sửa sau khi cài đặt
- Output files luôn lưu cùng thư mục Excel được chọn
- Nếu output đã tồn tại, ứng dụng sẽ thêm suffix `-01`, `-02`, v.v.

## Quick Start

1. **Chuẩn bị file:**
   ```
   project/
   ├── build-exe.bat
   ├── App.config
   ├── printCoverTemplate.docx
   ├── printLabelSaoHanTemplate.docx
   ├── printNotebookTemplate.docx
   ├── printSoSaoHanTemplate.docx
   └── ... (source files)
   ```

2. **Chạy build:**
   ```
   .\build-exe.bat
   ```

3. **Test ứng dụng:**
   ```
   target\dist\Sovan\Sovan.exe
   ```

4. **Distribute:**
   - Portable: Chia sẻ thư mục `target\dist\Sovan\`
   - EXE: Chia sẻ file `target\dist\Sovan-0.0.2.exe`

