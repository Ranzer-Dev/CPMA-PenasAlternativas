package dao;

import model.Administrador;
import database.ConnectionFactory;
import util.HashUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AdminDAO {

    public Administrador buscarPorCpf(String cpf) {
        Administrador admin = null;
        String sql = "SELECT * FROM Administrador WHERE cpf = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Normaliza o CPF removendo pontos e hífens para comparar
            String cpfNormalizado = cpf != null ? cpf.replaceAll("[^0-9]", "") : "";

            stmt.setString(1, cpfNormalizado);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                admin = new Administrador();
                admin.setIdAdministrador(rs.getInt("id_admin"));
                admin.setNome(rs.getString("nome"));
                admin.setCpf(rs.getString("cpf"));
                admin.setSenha(rs.getString("senha"));
                admin.setNivelPermissao(rs.getInt("nivel_permissao"));
                admin.setPerguntaSecreta(rs.getString("pergunta_secreta"));
                admin.setRespostaSecreta(rs.getString("resposta_secreta"));
            } else {
                System.out.println("Nenhum admin encontrado com o CPF: " + cpf);
            }

        } catch (Exception e) {
            System.err.println("Erro ao buscar admin por CPF: " + e.getMessage());
            e.printStackTrace();
        }

        return admin;
    }


    public boolean alterarSenhaPorCpf(String cpf, String novaSenha) {
        String sql = "UPDATE Administrador SET senha = ? WHERE cpf = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Normaliza o CPF removendo pontos e hífens
            String cpfNormalizado = cpf != null ? cpf.replaceAll("[^0-9]", "") : "";
            String hashSenha = HashUtil.gerarHash(novaSenha);
            stmt.setString(1, hashSenha);
            stmt.setString(2, cpfNormalizado);

            int linhasAfetadas = stmt.executeUpdate();
            return linhasAfetadas > 0;

        } catch (Exception e) {
            System.err.println("Erro ao atualizar senha do admin: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
