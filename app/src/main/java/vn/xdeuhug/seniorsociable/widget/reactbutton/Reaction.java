/*
 * MIT License
 *
 * Copyright (c) 2018 AmrDeveloper (Amr Hesham)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package vn.xdeuhug.seniorsociable.widget.reactbutton;

import java.util.Arrays;

/**
 * Lớp mô hình phản ứng dùng để lưu trữ thông tin cho từng phản ứng
 */
public class Reaction {

    private final String reactText;

    private final String reactType;

    private final String reactTextColor;

    private final int reactIconId;

    /**
     * Trình xây dựng này cho trạng thái mặc định vì loại React không bằng Văn bản phản ứng
     * ví dụ: trong thư viện văn bản trạng thái mặc định là 'thích' nhưng loại là 'mặc định'
     */
    public Reaction(String reactText, String reactType, String reactTextColor, int reactIconId) {
        this.reactText = reactText;
        this.reactType = reactType;
        this.reactTextColor = reactTextColor;
        this.reactIconId = reactIconId;
    }

    /**
     * Trình xây dựng cho tất cả các Phản ứng mà văn bản có kiểu bằng nhau
     * ví dụ ở trạng thái thích văn bản là 'thích' và loại cũng là 'thích'
     */
    public Reaction(String reactText, String reactTextColor, int reactIconId) {
        this.reactText = reactText;
        this.reactType = reactText;
        this.reactTextColor = reactTextColor;
        this.reactIconId = reactIconId;
    }

    public String getReactText() {
        return reactText;
    }

    public String getReactTextColor() {
        return reactTextColor;
    }

    public int getReactIconId() {
        return reactIconId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reaction reaction = (Reaction) o;
        return reactIconId == reaction.reactIconId &&
                (reactText != null && reactText.equals(reaction.reactText)) &&
                (reactType != null && reactType.equals(reaction.reactType)) &&
                (reactTextColor != null && reactTextColor.equals(reaction.reactTextColor));
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(new int[]{
                reactIconId,
                reactText.hashCode(),
                reactType.hashCode(),
                reactTextColor.hashCode(),
        });
    }
}
