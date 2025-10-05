package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import database.ConnectionFactory;
import model.Usuario;
import util.SessaoUsuario;

public class UsuarioDAO {

    public Usuario buscarPorCpf(String cpf) {
        String sql = "SELECT * FROM Usuario WHERE cpf = ?";

        try (Connection conn = ConnectionFactory.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cpf);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Usuario usuario = new Usuario();
                usuario.setIdUsuario(rs.getInt("id_usuario"));
                usuario.setNome(rs.getString("nome"));
                usuario.setCpf(rs.getString("cpf"));
                usuario.setSenha(rs.getString("senha"));
                usuario.setNacionalidade(rs.getString("nacionalidade"));
                usuario.setDataNascimento(rs.getDate("data_nascimento"));
                usuario.setCriadoEm(rs.getDate("criado_em"));
                usuario.setFoto(rs.getString("foto"));
                usuario.setEndereco(rs.getString("endereco"));
                usuario.setBairro(rs.getString("bairro"));
                usuario.setCidade(rs.getString("cidade"));
                usuario.setUf(rs.getString("uf"));
                usuario.setObservacao(rs.getString("observacao"));
                usuario.setTelefone(rs.getString("telefone"));
                usuario.setCep(rs.getString("cep"));
                usuario.setFkAdministradorIdAdmin(rs.getInt("fk_administrador_id_admin"));
                return usuario;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Usuario buscarPorNome(String nome) {
        String sql = "SELECT * FROM Usuario WHERE nome LIKE ?";

        try (Connection conn = ConnectionFactory.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + nome + "%");
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Usuario usuario = new Usuario();
                usuario.setIdUsuario(rs.getInt("id_usuario"));
                usuario.setNome(rs.getString("nome"));
                usuario.setCpf(rs.getString("cpf"));
                usuario.setSenha(rs.getString("senha"));
                usuario.setNacionalidade(rs.getString("nacionalidade"));
                usuario.setDataNascimento(rs.getDate("data_nascimento"));
                usuario.setCriadoEm(rs.getDate("criado_em"));
                usuario.setFoto(rs.getString("foto"));
                usuario.setEndereco(rs.getString("endereco"));
                usuario.setBairro(rs.getString("bairro"));
                usuario.setCidade(rs.getString("cidade"));
                usuario.setUf(rs.getString("uf"));
                usuario.setObservacao(rs.getString("observacao"));
                usuario.setTelefone(rs.getString("telefone"));
                usuario.setCep(rs.getString("cep"));
                usuario.setFkAdministradorIdAdmin(rs.getInt("fk_administrador_id_admin"));
                return usuario;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int inserir(Usuario usuario) {
        String sql = "INSERT INTO Usuario (nome, cpf, senha, nacionalidade, data_nascimento, "
                + "endereco, bairro, cidade, uf, observacao, foto, fk_Administrador_id_admin, telefone, cep, codigo) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            usuario.setFkAdministradorIdAdmin(SessaoUsuario.getAdminLogado().getIdAdministrador());

            stmt.setString(1, usuario.getNome());
            stmt.setString(2, usuario.getCpf());
            stmt.setString(3, usuario.getSenha());
            stmt.setString(4, usuario.getNacionalidade());
            stmt.setDate(5, new java.sql.Date(usuario.getDataNascimento().getTime()));
            stmt.setString(6, usuario.getEndereco());
            stmt.setString(7, usuario.getBairro());
            stmt.setString(8, usuario.getCidade());
            stmt.setString(9, usuario.getUf());
            stmt.setString(10, usuario.getObservacao());
            stmt.setString(11, usuario.getFoto());
            stmt.setInt(12, usuario.getFkAdministradorIdAdmin());
            stmt.setString(13, usuario.getTelefone());
            stmt.setString(14, usuario.getCep());
            stmt.setString(15, usuario.getCodigo());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static boolean atualizar(Usuario usuario) {
        String sql = "UPDATE Usuario SET nome = ?, cpf = ?, senha = ?, nacionalidade = ?, data_nascimento = ?, "
                + "endereco = ?, bairro = ?, cidade = ?, uf = ?, observacao = ?, foto = ?, telefone = ?, cep = ? , codigo = ?  WHERE id_usuario = ?";

        try (Connection conn = ConnectionFactory.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, usuario.getNome());
            stmt.setString(2, usuario.getCpf());
            stmt.setString(3, usuario.getSenha());
            stmt.setString(4, usuario.getNacionalidade());
            stmt.setDate(5, new java.sql.Date(usuario.getDataNascimento().getTime()));
            stmt.setString(7, usuario.getEndereco());
            stmt.setString(8, usuario.getBairro());
            stmt.setString(9, usuario.getCidade());
            stmt.setString(10, usuario.getUf());
            stmt.setString(11, usuario.getObservacao());
            stmt.setString(12, usuario.getFoto());
            stmt.setString(13, usuario.getTelefone());
            stmt.setString(14, usuario.getCep());
            stmt.setString(15, usuario.getCodigo());
            stmt.setInt(16, usuario.getIdUsuario());

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static List<Usuario> buscarTodosUsuarios() {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT * FROM usuario";

        try (Connection conn = ConnectionFactory.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Usuario u = new Usuario();
                u.setIdUsuario(rs.getInt("id_usuario"));
                u.setFkAdministradorIdAdmin(rs.getInt("fk_administrador_id_admin"));
                u.setNome(rs.getString("nome"));
                u.setCpf(rs.getString("cpf"));
                u.setDataNascimento(rs.getDate("data_nascimento"));
                u.setEndereco(rs.getString("endereco"));
                u.setBairro(rs.getString("bairro"));
                u.setCidade(rs.getString("cidade"));
                u.setUf(rs.getString("uf"));
                u.setNacionalidade(rs.getString("nacionalidade"));
                u.setCriadoEm(rs.getDate("data_cadastro"));
                u.setFoto(rs.getString("foto"));
                u.setSenha(rs.getString("senha"));
                u.setObservacao(rs.getString("observacao"));
                u.setTelefone(rs.getString("telefone"));
                u.setCep(rs.getString("cep"));
                u.setCodigo(rs.getString("codigo"));
                lista.add(u);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public static boolean cpfExiste(String cpf) {
        String sql = "SELECT COUNT(*) FROM Usuario WHERE cpf = ?";
        try (Connection conn = ConnectionFactory.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cpf);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static int buscarUltimoIdInserido() {
        String sql = "SELECT MAX(id_usuario) AS ultimo_id FROM Usuario";
        try (Connection conn = ConnectionFactory.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("ultimo_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
}
