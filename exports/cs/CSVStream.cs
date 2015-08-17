using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Config;
using Microsoft.SqlServer.Server;

namespace cfg
{
    public class CSVStream
    {
        private readonly string EOL = "##";
        private readonly string END = "]]";

        private readonly List<List<string>> lines;
        private int row;
        private int col;
        CSVStream(string file, string encoding)
        {
            using (var reader = new StreamReader(file, Encoding.GetEncoding(encoding)))
            {
                lines = CSV.Parse(reader);
            }
            row = 0;
            col = 0;
        }

        public string GetNext()
        {
            while (true)
            {
                if (row >= lines.Count) return null;
                var line = lines[row];
                if (col >= line.Count)
                {
                    row++;
                    col = 0;
                    continue;
                }
                var data = line[col++];
                if (data.StartsWith(EOL))
                {
                    row++;
                    col = 0;
                    continue;
                }
                if (data.Length > 0)
                {
                    return data;
                }
            }
        }

        public bool IsSectionEnd()
        {
            while (true)
            {
                if (row >= lines.Count) return true;
                var line = lines[row];
                if (col >= line.Count)
                {
                    row++;
                    col = 0;
                    continue;
                }
                var data = line[col];
                if (data.StartsWith(EOL))
                {
                    row++;
                    col = 0;
                    continue;
                }
                if (data.Length > 0)
                {
                    if (data.StartsWith(END))
                    {
                        col++;
                        return true;
                    }
                    else
                    {
                        return false;
                    }
                }
                else
                {
                    col++;
                }
            }
        }

        void Error(string err)
        {
            throw new Exception(err);
        }

        string GetNextAndCheckNotEmpty()
        {
            var s = GetNext();
            if (s == null)
                Error("read not enough");
            return s;
        }

        public string GetString()
        {
            var s = GetNextAndCheckNotEmpty();
            return s.Replace("\\#", "#").Replace("\\]", "]").Replace("\\s", "").Replace("\\\\", "\\");
        }

        public float GetFloat()
        {
            return float.Parse(GetNextAndCheckNotEmpty());
        }


        public int GetInt()
        {
            return int.Parse(GetNextAndCheckNotEmpty());
        }

        public bool GetBool()
        {
            var s = GetNextAndCheckNotEmpty().ToLower();
            if (s == "true")
                return true;
            if (s == "false")
                return false;
            Error(s + " isn't bool");
            return false;
        }

        public static CSVStream Create(string file, string encoding)
        {
            return new CSVStream(file, encoding);

        }
    }
}
