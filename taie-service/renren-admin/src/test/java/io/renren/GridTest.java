package io.renren;

public class GridTest {
    public static void main(String[] args) {
        int rectX = 70;      // 矩形左上角X坐标
        int rectY = 774;     // 矩形左上角Y坐标
        int rectHeight = 580;  // 矩形高度
        int rectWidth = 580;   // 矩形宽度

        // 计算九宫格（9个正方形格子）
        // 布局规则：
        // - 格子距离边框的距离为 x
        // - 格子之间的距离为 2x
        // - 格子的宽度约等于 3x (gridSize ≈ 3x)
        // 
        // 布局示意：
        // x + gridSize + 2x + gridSize + 2x + gridSize + x = width
        // 即：6x + 3*gridSize = width
        // 由于 gridSize ≈ 3x，代入得：
        // 6x + 3*(3x) = width
        // 6x + 9x = width
        // 15x = width
        // 所以：x = width / 15
        
        // 根据矩形宽度计算边框间距 x
        int x = rectWidth / 15;  // 边框间距
        
        // 计算格子大小
        int gridSize = (rectWidth - 6 * x) / 3;
        
        System.out.println("九宫格计算结果：");
        System.out.println("====================================");
        System.out.println("矩形位置：(" + rectX + ", " + rectY + ")");
        System.out.println("矩形大小：" + rectWidth + " x " + rectHeight);
        System.out.println("边框间距 x：" + x);
        System.out.println("格子间距：" + (2 * x));
        System.out.println("格子大小：" + gridSize + " x " + gridSize);
        System.out.println("比例验证：gridSize / x = " + String.format("%.2f", (double)gridSize / x) + " (约等于3)");
        System.out.println("====================================\n");
        
        // 计算9个格子的坐标（格子的左上角坐标）
        System.out.println("九宫格格子坐标（左上角位置）：\n");
        
        // 存储格子信息：[格子编号][x坐标, y坐标, 宽度, 高度]
        int[][] grids = new int[9][4];
        int gridNum = 1;
        
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                // 计算当前格子左上角的坐标
                // 第0列：rectX + x
                // 第1列：rectX + x + gridSize + 2x = rectX + 3x + gridSize
                // 第2列：rectX + x + gridSize + 2x + gridSize + 2x = rectX + 5x + 2*gridSize
                int gridX = rectX + x + col * (gridSize + 2 * x);
                int gridY = rectY + x + row * (gridSize + 2 * x);
                
                grids[gridNum - 1][0] = gridX;
                grids[gridNum - 1][1] = gridY;
                grids[gridNum - 1][2] = gridSize;
                grids[gridNum - 1][3] = gridSize;
                
                System.out.println("格子" + gridNum + ": 位置(" + gridX + ", " + gridY + "), 大小(" + gridSize + " x " + gridSize + ")");
                gridNum++;
            }
        }
        
        // 也计算每个格子的中心点坐标（用于触摸检测）
        System.out.println("\n九宫格中心点坐标：\n");
        for (int i = 0; i < 9; i++) {
            int centerX = grids[i][0] + gridSize / 2;
            int centerY = grids[i][1] + gridSize / 2;
            System.out.println("格子" + (i + 1) + " 中心: (" + centerX + ", " + centerY + ")");
        }
        
        // 输出数组格式（便于代码使用）
        System.out.println("\n数组格式（左上角坐标）：");
        System.out.println("int[][] gridPositions = {");
        for (int i = 0; i < 9; i++) {
            System.out.println("    {" + grids[i][0] + ", " + grids[i][1] + ", " + grids[i][2] + ", " + grids[i][3] + "}" + (i < 8 ? "," : "") + " // 格子" + (i + 1) + " (x, y, width, height)");
        }
        System.out.println("};");
        
    }
}
