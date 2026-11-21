package gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;

/**
 * Cores, fontes e fábricas de componentes com a aparência do sistema.
 * Concentrar o estilo aqui mantém as telas simples e consistentes.
 */



public final class Tema {

        public static JTextField campo() {
        return campoTexto();
    }


    public static final Color PRIMARIA = new Color(0x0E7C7B);
    public static final Color PRIMARIA_HOVER = new Color(0x0B6362);
    public static final Color MENU_FUNDO = new Color(0x12343B);
    public static final Color MENU_HOVER = new Color(0x1B4A54);
    public static final Color MENU_SELECIONADO = new Color(0x1F5C68);
    public static final Color DESTAQUE = new Color(0xF2B134);
    public static final Color FUNDO = new Color(0xF3F6F7);
    public static final Color CARTAO = Color.WHITE;
    public static final Color BORDA = new Color(0xD3DDE1);
    public static final Color TEXTO = new Color(0x22313A);
    public static final Color TEXTO_SUAVE = new Color(0x667882);
    public static final Color PERIGO = new Color(0xC0392B);
    public static final Color PERIGO_HOVER = new Color(0xA03024);
    public static final Color CAMPO_BLOQUEADO = new Color(0xEBF0F2);
    private static final Color CABECALHO_TABELA = new Color(0xE6EEF0);
    private static final Color LINHA_ZEBRA = new Color(0xF7FAFB);
    private static final Color SELECAO = new Color(0xCDE7E6);
    private static final Color BOTAO_DESABILITADO = new Color(0xE3E8EA);

    public static final Font FONTE = new Font(Font.SANS_SERIF, Font.PLAIN, 13);
    public static final Font FONTE_NEGRITO = new Font(Font.SANS_SERIF, Font.BOLD, 13);

    private Tema() {
    }

    /** Aplica a fonte padrão a todos os componentes Swing (deve ser chamado antes de criar telas). */
    public static void instalarFontePadrao() {
        FontUIResource fonte = new FontUIResource(FONTE);
        UIManager.put("defaultFont", fonte);
        Enumeration<Object> chaves = UIManager.getDefaults().keys();
        while (chaves.hasMoreElements()) {
            Object chave = chaves.nextElement();
            if (chave.toString().endsWith(".font")) {
                UIManager.put(chave, fonte);
            }
        }
    }

    // ------------------------------------------------------------------ textos

    public static JLabel titulo(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        l.setForeground(TEXTO);
        return l;
    }

    public static JLabel subtitulo(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(FONTE);
        l.setForeground(TEXTO_SUAVE);
        return l;
    }

    public static JLabel secao(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        l.setForeground(TEXTO);
        return l;
    }

    public static JLabel rotulo(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(FONTE_NEGRITO);
        l.setForeground(TEXTO);
        return l;
    }

    // ------------------------------------------------------------------ contêineres

    public static JPanel cartao() {
        JPanel p = new JPanel();
        p.setBackground(CARTAO);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDA, 1),
                BorderFactory.createEmptyBorder(18, 20, 18, 20)
        ));
        return p;
    }

    // ------------------------------------------------------------------ botões

    public static JButton botaoPrimario(String texto) {
        return criarBotao(texto, PRIMARIA, PRIMARIA_HOVER, Color.WHITE);
    }

    public static JButton botaoPerigo(String texto) {
        return criarBotao(texto, PERIGO, PERIGO_HOVER, Color.WHITE);
    }

    public static JButton botaoSecundario(String texto) {
        JButton b = criarBotao(texto, CARTAO, CABECALHO_TABELA, TEXTO);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDA, 1),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));
        return b;
    }

    private static JButton criarBotao(String texto, Color fundo, Color hover, Color corTexto) {
        JButton b = new JButton(texto) {
            @Override
            public void setEnabled(boolean enabled) {
                super.setEnabled(enabled);
                setBackground(enabled ? fundo : BOTAO_DESABILITADO);
                setForeground(enabled ? corTexto : TEXTO_SUAVE);
            }
        };
        b.setFont(FONTE_NEGRITO);
        b.setBackground(fundo);
        b.setForeground(corTexto);
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setBorder(BorderFactory.createEmptyBorder(9, 18, 9, 18));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        b.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (b.isEnabled()) {
                    b.setBackground(hover);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (b.isEnabled()) {
                    b.setBackground(fundo);
                }
            }
        });
        return b;
    }

    // ------------------------------------------------------------------ campos

    public static JTextField campoTexto() {
        JTextField tf = new JTextField();
        estilizarCampo(tf);
        return tf;
    }

    public static void estilizarCampo(JComponent campo) {
        campo.setFont(FONTE);
        campo.setForeground(TEXTO);
        campo.setBackground(Color.WHITE);
        campo.setPreferredSize(new Dimension(200, 34));
        Border bordaInterna = BorderFactory.createEmptyBorder(5, 10, 5, 10);
        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDA, 1),
                bordaInterna
        ));
    }

    public static void bloquearCampo(JTextField campo, boolean bloquear) {
        campo.setEditable(!bloquear);
        campo.setBackground(bloquear ? CAMPO_BLOQUEADO : Color.WHITE);
    }

    // ------------------------------------------------------------------ tabelas
        public static JTable tabela(TableModel model) {
        JTable table = new JTable(model);
        table.setFont(FONTE);
        table.setForeground(TEXTO);
        table.setRowHeight(32);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(SELECAO);
        table.setSelectionForeground(TEXTO);

        table.getTableHeader().setFont(FONTE_NEGRITO);
        table.getTableHeader().setBackground(CABECALHO_TABELA);
        table.getTableHeader().setForeground(TEXTO);
        table.getTableHeader().setPreferredSize(new Dimension(0, 36));
        table.getTableHeader().setReorderingAllowed(false);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : LINHA_ZEBRA);
                }
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return c;
            }
        });
        return table;
    }

    public static JScrollPane rolagem(JTable table) {
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDA, 1));
        scroll.getViewport().setBackground(Color.WHITE);
        return scroll;
    }

    public static JScrollPane tabela(JTable table) {
        table.setFont(FONTE);
        table.setForeground(TEXTO);
        table.setRowHeight(32);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(SELECAO);
        table.setSelectionForeground(TEXTO);

        table.getTableHeader().setFont(FONTE_NEGRITO);
        table.getTableHeader().setBackground(CABECALHO_TABELA);
        table.getTableHeader().setForeground(TEXTO);
        table.getTableHeader().setPreferredSize(new Dimension(0, 36));
        table.getTableHeader().setReorderingAllowed(false);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : LINHA_ZEBRA);
                }
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return c;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDA, 1));
        scroll.getViewport().setBackground(Color.WHITE);
        return scroll;
    }

    public static JTable criarTabela(TableModel model) {
        return new JTable(model);
    }

    // ------------------------------------------------------------------ layout

    public static GridBagConstraints gbc(int x, int y) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = x;
        c.gridy = y;
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        return c;
    }
}
